# Sistema financiero: clientes, cuentas y movimientos

Aplicación para los trabajadores de una entidad financiera. Permite administrar clientes, abrirles cuentas de ahorros o corrientes y registrar consignaciones, retiros y transferencias, con consulta del estado de cuenta.

## Tecnologías

| Parte | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Data JPA, Bean Validation, Maven |
| Base de datos | PostgreSQL (tablas creadas con migraciones Flyway) |
| Frontend | Angular |
| Pruebas | JUnit 5, Mockito |
| Contenedores | Docker y Docker Compose |

## Arquitectura

El backend usa **arquitectura hexagonal**: las reglas del banco viven en el centro (dominio y casos de uso) y no dependen de la web ni de la base de datos. Los controladores REST y la persistencia con JPA son adaptadores que se conectan a ese centro a través de puertos (interfaces).

## Ramas

| Rama | Uso |
| --- | --- |
| `main` | Versión estable entregable |
| `develop` | Integración de funcionalidades terminadas |
| `feature/*` | Una rama por funcionalidad (`feature/clients`, `feature/accounts`, `feature/transactions`, ...) |

## Estado

En construcción. Las instrucciones para ejecutar el proyecto se agregarán a medida que avancen las etapas.
