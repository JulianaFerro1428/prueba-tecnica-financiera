# Sistema financiero: clientes, cuentas y movimientos

Aplicación para los trabajadores de una entidad financiera. Permite administrar clientes, abrirles cuentas de ahorros o corrientes y registrar consignaciones, retiros y transferencias, con consulta del estado de cuenta.

## Tecnologías

| Parte | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Data JPA, Bean Validation, Maven |
| Base de datos | PostgreSQL (tablas creadas con migraciones Flyway) |
| Frontend | Angular 21, TypeScript, nginx (en Docker) |
| Pruebas | JUnit 5, Mockito, Testcontainers (backend); Vitest (frontend) |
| Contenedores | Docker y Docker Compose |

## Arquitectura

El backend usa **arquitectura hexagonal**: las reglas del banco viven en el centro (dominio y casos de uso) y no dependen de la web ni de la base de datos. Los controladores REST y la persistencia con JPA son adaptadores que se conectan a ese centro a través de puertos (interfaces).

## Ramas

| Rama | Uso |
| --- | --- |
| `main` | Versión estable entregable |
| `develop` | Integración de funcionalidades terminadas |
| `feature/*` | Una rama por funcionalidad (`feature/clients`, `feature/accounts`, `feature/transactions`, ...) |

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

## Estado

En construcción. Las instrucciones se completarán a medida que avancen las etapas.
