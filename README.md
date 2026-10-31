# Sistema financiero: clientes, cuentas y movimientos

Aplicación para los trabajadores de una entidad financiera. Permite administrar clientes, abrirles cuentas de ahorros o corrientes y registrar consignaciones, retiros y transferencias, con consulta del estado de cuenta.

## Tecnologías

| Parte | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Data JPA, Bean Validation, Maven |
| Base de datos | PostgreSQL (tablas creadas con migraciones Flyway) |
| Frontend | Angular 21, TypeScript, nginx (en Docker) |
| Pruebas | JUnit 5, Mockito, Testcontainers y JaCoCo (backend); Vitest (frontend) |
| Contenedores | Docker y Docker Compose |
| Nube | AWS EC2 (una máquina con el mismo Docker Compose) |

## Ejecución con Docker (todo junto)

Requisito: Docker Desktop. No hace falta tener Java, Maven ni Node instalados.

1. Crear el archivo de variables a partir del ejemplo (solo la primera vez):
   ```bash
   cp .env.example .env
   ```
2. Construir las imágenes y levantar los tres servicios:
   ```bash
   docker compose up -d --build
   ```
3. Abrir la aplicación en http://localhost:8090

| Servicio | Contenedor | Puerto en tu máquina | Qué hace |
| --- | --- | --- | --- |
| `frontend` | `bank-frontend` | `8090` | nginx sirve la app de Angular y reenvía `/api` al backend |
| `backend` | `bank-backend` | `8082` | API REST de Spring Boot (útil para probarla con curl o Postman) |
| `db` | `bank-db` | `5433` | PostgreSQL; los datos se guardan en el volumen `db-data` |

Comandos útiles:

```bash
docker compose ps                 # estado de los contenedores
docker compose logs -f backend    # ver los registros del backend en vivo
docker compose up -d --build      # reconstruir después de cambiar código
docker compose down               # detener y borrar contenedores (los datos se conservan)
```

`docker compose down -v` también borra el volumen, es decir, **todos los datos** de la base de datos.

## Despliegue en AWS

La aplicación corre en **una sola instancia EC2** (un servidor alquilado a AWS) que ejecuta el mismo `docker-compose.yml`. Se eligió así porque reutiliza lo que ya funciona en local, cabe en el plan gratuito y se borra en minutos; para más usuarios se migraría a servicios administrados (RDS, ECS) sin cambiar el código, porque la conexión a la base de datos llega por variables de entorno.

```text
Navegador ──http:80──► EC2 t3.small (Ubuntu 24.04, región us-east-2)
                       └─ Docker Compose: frontend (nginx) ──/api──► backend ──► db
```

| Recurso | Configuración | Por qué |
| --- | --- | --- |
| Instancia EC2 `bank-app` | `t3.small` (2 GB RAM + 2 GB de swap), modo de CPU *standard* | Spring Boot y PostgreSQL no caben con holgura en 1 GB; *standard* evita cobros por uso extra de CPU |
| Disco | 20 GB gp3, cifrado | Guarda las imágenes y el volumen de la base de datos |
| Security Group `bank-app-sg` | Puerto 80 abierto a internet; 22 (SSH) solo desde la IP del administrador | La API (8082) y PostgreSQL (5433) no son accesibles desde fuera |
| Metadatos | IMDSv2 obligatorio | Impide que una petición maliciosa robe credenciales de la máquina |
| Llave SSH `bank-ec2` | Guardada fuera del repositorio | Acceso de administración al servidor |

**Cómo se desplegó:**

1. Construir las imágenes en local y empaquetarlas (compilar en un servidor de 2 GB es lento):
   ```bash
   docker compose build
   docker save prueba-backend:latest prueba-frontend:latest | gzip > bank-images.tar.gz
   ```
2. Copiarlas al servidor junto con el Compose:
   ```bash
   scp -i ~/.ssh/bank-ec2.pem bank-images.tar.gz docker-compose.yml ubuntu@<IP-publica>:prueba/
   ```
3. En el servidor, cargar las imágenes, crear el `.env` con una contraseña aleatoria (generada allí, nunca en Git) y levantar:
   ```bash
   gunzip -c bank-images.tar.gz | docker load
   docker compose up -d --no-build
   ```
   El `.env` del servidor usa `FRONTEND_PORT=80` para que la app abra en `http://<IP-publica>`.

**Costo y limpieza:** la cuenta usa el plan gratuito de AWS (créditos, sin cobro a la tarjeta). La instancia consume créditos mientras está encendida; detenerla deja de consumirlos salvo el disco, pero cambia la IP pública. Para borrar todo:

```bash
aws ec2 terminate-instances --instance-ids <id-instancia>
aws ec2 wait instance-terminated --instance-ids <id-instancia>
aws ec2 delete-security-group --group-name bank-app-sg
aws ec2 delete-key-pair --key-name bank-ec2
```

## Ejecución local (desarrollo)

Requisitos: Java 21, Maven 3.9+, Node 22 y Docker Desktop.

1. Crear el archivo de variables a partir del ejemplo (solo la primera vez):
   ```bash
   cp .env.example .env
   ```
2. Levantar PostgreSQL (puerto `5433`):
   ```bash
   docker compose up -d db
   ```
3. Ejecutar el backend (puerto `8081`):
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Ejecutar el frontend (puerto `4300`); `proxy.conf.json` reenvía `/api` al backend del paso 3:
   ```bash
   cd frontend
   npm install
   npx ng serve
   ```

Al arrancar, Flyway aplica los scripts de `backend/src/main/resources/db/migration` para crear o actualizar las tablas.

## Pruebas

**Backend** (necesita Docker encendido: la prueba de integración levanta un PostgreSQL temporal con Testcontainers):

```bash
cd backend
mvn test
```

- 78 pruebas: dominio, servicios (casos de uso con Mockito), controladores (`@WebMvcTest`) y una prueba de integración de la transferencia contra PostgreSQL real.
- El informe de cobertura queda en `backend/target/site/jacoco/index.html` (ábrelo en el navegador). En la ejecución del 30/10/2026 la cobertura de líneas fue **94,7 %** (501 de 529); el número cambia si cambia el código, así que conviene volver a generarlo.

**Frontend:**

```bash
cd frontend
npx ng test --watch=false
```

- 31 pruebas de componentes y servicios con `HttpTestingController`, que simula las respuestas del backend.

## API REST

Todas las rutas empiezan por `/api`. Los errores se responden en formato `ProblemDetail` con el motivo en el campo `detail`.

| Método | Ruta | Qué hace | Respuesta correcta |
| --- | --- | --- | --- |
| `POST` | `/clients` | Registra un cliente | `201` |
| `GET` | `/clients` | Lista los clientes | `200` |
| `GET` | `/clients/{id}` | Consulta un cliente | `200` |
| `PUT` | `/clients/{id}` | Actualiza un cliente | `200` |
| `DELETE` | `/clients/{id}` | Elimina un cliente sin cuentas | `204` |
| `POST` | `/accounts` | Abre una cuenta (`clientId`, `type`, `gmfExempt`) | `201` |
| `GET` | `/accounts` | Lista las cuentas (`?clientId=` para las de un cliente) | `200` |
| `GET` | `/accounts/{id}` | Consulta una cuenta | `200` |
| `PATCH` | `/accounts/{id}/activate` | Activa la cuenta | `200` |
| `PATCH` | `/accounts/{id}/deactivate` | Inactiva la cuenta | `200` |
| `PATCH` | `/accounts/{id}/cancel` | Cancela la cuenta (solo con saldo $0) | `200` |
| `POST` | `/accounts/{id}/deposits` | Consigna (`amount`) | `201` |
| `POST` | `/accounts/{id}/withdrawals` | Retira (`amount`) | `201` |
| `POST` | `/transfers` | Transfiere (`sourceAccountId`, `targetAccountId`, `amount`) | `201` |
| `GET` | `/accounts/{id}/statement` | Estado de cuenta: cuenta y movimientos | `200` |

Códigos de error: `400` datos con formato inválido, `404` cliente o cuenta inexistente, `409` dato repetido (documento o cuenta del mismo tipo), `422` regla del banco incumplida (menor de edad, saldo insuficiente, cuenta inactiva...).

## Arquitectura

El backend usa **arquitectura hexagonal**: las reglas del banco viven en el centro (dominio y casos de uso) y no dependen de la web ni de la base de datos. Los controladores REST y la persistencia con JPA son adaptadores que se conectan a ese centro a través de puertos (interfaces). Así se podría cambiar PostgreSQL o la API REST sin reescribir las reglas.

```text
backend/src/main/java/com/bank/
├── domain/                     Reglas del banco en Java puro (sin Spring ni JPA)
│   ├── model/                  Client, Account, Movement, AccountNumber...
│   └── exception/              Reglas incumplidas: UnderageClientException, InsufficientFundsException...
├── application/
│   ├── port/in/                Casos de uso que ofrece el sistema: OpenAccountUseCase, TransferUseCase...
│   ├── port/out/               Lo que el sistema necesita de afuera: AccountRepositoryPort...
│   └── service/                Implementan los casos de uso: ClientService, AccountService, TransactionService
└── infrastructure/
    ├── adapter/in/rest/        Controladores, DTOs y manejo global de errores
    ├── adapter/out/persistence/ Entidades JPA, repositorios Spring Data, mappers y adaptadores de los puertos
    └── config/                 Configuración (por ejemplo, el reloj del sistema)
```

Equivalencia con las capas que pide el enunciado:

| Capa del enunciado | Dónde está |
| --- | --- |
| Entity | `domain/model` (reglas) y `persistence/entity` (tablas JPA), separadas a propósito |
| Service | `application/service` |
| Controller | `adapter/in/rest/controller` |
| Repository | Puertos en `application/port/out`, implementados en `persistence/adapter` con Spring Data |

Recorrido de una transferencia de $50.000:

1. En Angular, `TransferForm` envía `POST /api/transfers` con `TransactionService` (frontend).
2. `TransactionController` valida el formato del JSON y llama al puerto `TransferUseCase`.
3. `TransactionService` abre una transacción (`@Transactional`) y bloquea las dos cuentas en orden de id, para que dos operaciones simultáneas no lean el mismo saldo.
4. El dominio (`Account`) resta en el origen y suma en el destino, y produce dos movimientos (débito y crédito) con la misma referencia.
5. `AccountRepositoryPort` y `MovementRepositoryPort` guardan todo a través de sus adaptadores JPA en PostgreSQL.
6. Si cualquier paso falla, la transacción se deshace completa: nunca se debita una cuenta sin acreditar la otra (lo comprueba `TransferIntegrationTest`).

El frontend está organizado por funcionalidad (`features/clients`, `features/accounts`, `features/transactions`), cada una con sus páginas, componentes, servicios y modelos. Los servicios son los únicos que llaman al backend.

## Base de datos

Tres tablas creadas con Flyway (`V1` a `V4`):

- `clients`: documento único por tipo y número (`uk_clients_identification`) y restricción de mayoría de edad.
- `accounts`: número único de 10 dígitos que empieza por 53 (ahorros) o 33 (corriente), saldo no negativo, cancelación solo con saldo $0, clave foránea al cliente (`ON DELETE RESTRICT`) y una cuenta no cancelada por tipo y cliente.
- `movements`: cada movimiento con su cuenta, tipo, monto positivo y saldo resultante; las dos líneas de una transferencia comparten `reference`.

El dinero se guarda como `NUMERIC(19,2)` en PostgreSQL y `BigDecimal` en Java.

## Requisitos del enunciado

| Requisito | Dónde se cumple | Prueba que lo demuestra |
| --- | --- | --- |
| Crear, consultar, actualizar y eliminar clientes | `ClientController`, `ClientService`; pantallas Clientes y formulario de cliente | `ClientServiceTest`, `ClientControllerTest` |
| Fechas de creación y modificación automáticas | `Client` (dominio) con el reloj del sistema | `create_newAdultClient_isSavedWithAutomaticDates`, `update_existingClient_changesDataAndModificationDate` |
| No crear ni mantener clientes menores de edad | `Client`, restricción `ck_clients_adult` | `create_underageClient_isRejectedAndNotSaved`, `update_birthDateThatMakesClientUnderage_isRejectedAndKeepsOldData` |
| No eliminar clientes con productos | `ClientService.delete`, FK `ON DELETE RESTRICT` | `delete_clientWithAccounts_isRejectedAndNotDeleted` |
| Correo con formato válido y nombres de al menos 2 caracteres (opcional) | `ClientRequest` (Bean Validation) | `create_invalidEmailAndShortName_returns400AndDoesNotCallUseCase` |
| Solo cuentas de ahorros y corriente, siempre con cliente existente | `AccountType`, `AccountService.open` | `open_unknownClient_isRejectedWithoutUsingSequenceOrSaving` |
| Número único, automático, de 10 dígitos con prefijo 53/33 | `AccountNumber`, secuencias de PostgreSQL | `AccountNumberTest`, `open_existingClient_generatesNumberWithPrefixAndSavesActiveAccount` |
| Cuenta de ahorros activa al crearse | `Account.open` | `open_savingsAccount_startsActiveWithZeroBalance` |
| Ahorros sin saldo negativo | `Account` (débito), `ck_accounts_non_negative_balance` | `withdraw_moreThanBalance_isRejectedAndBalanceDoesNotChange` |
| Activar o inactivar en cualquier momento | `PATCH /accounts/{id}/activate` y `/deactivate`; botones en Cuentas | `deactivateAndActivate_changeStatusAndModificationDate` |
| Cancelar solo con saldo $0 | `Account.cancel`, `ck_accounts_cancelled_with_zero` | `cancel_withBalance_isRejectedAndKeepsStatus`, `cancel_withZeroBalance_cancelsAccount` |
| Consignación, retiro y transferencia con saldo actualizado | `TransactionService`; estado de cuenta y pantalla Transferir | `TransactionServiceTest`, `TransactionControllerTest` |
| Transferencia solo entre cuentas existentes, con débito y crédito | `TransactionService.transfer` | `transfer_validAccounts_movesMoneyAndRecordsDebitAndCreditWithSameReference` |
| Transferencia todo o nada | `@Transactional` en `TransactionService` | `TransferIntegrationTest` (simula un fallo a mitad de camino) |
| Consultar el estado de cuenta | `GET /accounts/{id}/statement`; pantalla Estado de cuenta | `statement_returnsAccountAndMovements`, `account-statement.spec.ts` |
| Pruebas unitarias de Service y Controller | `backend/src/test` | 78 pruebas, cobertura con JaCoCo |
| Frontend conectado al backend | `frontend/` | 31 pruebas con Vitest |
| Docker para frontend, backend y base de datos | `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` | `docker compose up -d --build` |

## Decisiones propias

El enunciado no define estos puntos; estas son las decisiones tomadas y su motivo:

| Tema | Decisión | Motivo |
| --- | --- | --- |
| "CRUD de transacciones" | Solo se crean y consultan; no se editan ni se borran | Modificar dinero ya movido rompería el historial del estado de cuenta |
| Saldo y saldo disponible | Se guardan los dos y siempre son iguales | El enunciado no define retenciones ni bloqueos de dinero |
| Saldo negativo en cuenta corriente | Tampoco se permite | El enunciado no define sobregiro ni cupo |
| Estado inicial de la cuenta corriente | Activa, igual que la de ahorros | Se puede usar de inmediato; se puede inactivar después |
| Movimientos en cuentas inactivas o canceladas | Se rechazan | Protege cuentas que el banco detuvo o cerró |
| Reapertura de cuentas canceladas | No se permite | La cancelación es el cierre definitivo |
| Saldo al abrir la cuenta | $0; se carga con una consignación | El enunciado no pide un depósito inicial |
| Documento del cliente | La combinación tipo + número es única; tipos CC, CE y PA | Evita clientes duplicados; la TI es de menores de edad |
| Cuentas por cliente | Una de cada tipo que no esté cancelada | Las inactivas cuentan porque se pueden reactivar |
| Inicio de sesión | No se implementó | El enunciado no lo pide |

## Ramas

| Rama | Uso |
| --- | --- |
| `main` | Versión estable entregable |
| `develop` | Integración de funcionalidades terminadas |
| `feature/*` | Una rama por funcionalidad, fusionada en `develop` con un pull request |
