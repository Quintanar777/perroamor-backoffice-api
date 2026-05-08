# Perro Amor — Inventory Backend

Backend del sistema de inventario y POS (point-of-sale) de Perro Amor.

## Stack

- Java 25 (LTS)
- Spring Boot 4.0.5 / Spring 7
- Gradle Kotlin DSL (wrapper 9.4.1)
- PostgreSQL 15 (dev: docker compose; prod: gestionado)
- Flyway 11.x — migraciones versionadas
- MapStruct 1.6 — mapeo dominio ↔ JPA
- springdoc-openapi 2.8 — contrato OpenAPI + Swagger UI
- JWT vía Spring Security 7 oauth2-resource-server (HS256)

## Arquitectura — capas y bounded contexts

```
src/main/java/com/perroamor/inventory/
├── InventoryApplication.java
├── shared/                    # cross-cutting (errores, tipos, config)
│   ├── error/                 # DomainException sealed + GlobalExceptionHandler (RFC 7807)
│   ├── config/                # CORS, OpenAPI, JpaAuditing
│   └── types/                 # Page, PageRequest, PagedResponse
├── auth/                      # autenticación (users, roles, JWT)
├── catalog/                   # marcas, productos, variantes
├── events/                    # eventos donde ocurren ventas
└── sales/                     # ventas (POS) + stats
```

Cada bounded context tiene `domain/` (records + ports, sin Spring), `application/` (services, casos de uso) e `infrastructure/` (persistence JPA, web REST). Reglas detalladas en [`docs/backend-plan.md`](docs/backend-plan.md).

## Levantar local

### 1. Postgres

```bash
docker compose up -d
```

Postgres 15 en `localhost:5432`, DB `inventory`, user/pass `inventory`/`inventory`.

### 2. Aplicación

```bash
./gradlew bootRun
```

App en `http://localhost:8080` con perfil `local` por default. Flyway aplica todas las migraciones al arrancar.

### 3. Verificar

- Health detallado: <http://localhost:8080/actuator/health>
- Info: <http://localhost:8080/actuator/info>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

### Login inicial (seed)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**IMPORTANTE**: cambiar el password del admin antes de cualquier deploy a producción.

## Endpoints

Todas las rutas están bajo `/api/v1`. Auth necesario salvo `/auth/login`, `/auth/refresh` y los públicos del actuator/swagger.

### Auth (`/api/v1/auth`)

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/login` | público | Devuelve access + refresh + datos del usuario |
| POST | `/refresh` | público | Emite tokens nuevos a partir del refresh token |
| GET  | `/me` | autenticado | Datos del usuario actual |
| POST | `/logout` | autenticado | No-op (cliente borra el token) |

### Brands (`/api/v1/brands`)

| Método | Ruta | Rol |
|---|---|---|
| GET    | `/` | autenticado |
| GET    | `/{id}` | autenticado |
| POST   | `/` | ADMIN, MANAGER |
| PUT    | `/{id}` | ADMIN, MANAGER |
| DELETE | `/{id}` | ADMIN (soft delete) |

### Products (`/api/v1/products`)

| Método | Ruta | Rol |
|---|---|---|
| GET    | `?brandId=&category=&q=&isActive=&page=&size=` | autenticado, paginado |
| GET    | `/{id}` | autenticado |
| POST   | `/` | ADMIN, MANAGER |
| PUT    | `/{id}` | ADMIN, MANAGER |
| DELETE | `/{id}` | ADMIN (soft delete) |
| PATCH  | `/{id}/stock` | ADMIN, MANAGER (ajuste manual con `delta` o `setTo`) |

### Variants

| Método | Ruta | Rol |
|---|---|---|
| GET    | `/api/v1/products/{productId}/variants` | autenticado |
| GET    | `/api/v1/variants/{id}` | autenticado |
| POST   | `/api/v1/products/{productId}/variants` | ADMIN, MANAGER |
| PUT    | `/api/v1/variants/{id}` | ADMIN, MANAGER |
| DELETE | `/api/v1/variants/{id}` | ADMIN (soft delete) |

### Events (`/api/v1/events`)

| Método | Ruta | Rol |
|---|---|---|
| GET    | `?status=&isActive=&page=&size=` | autenticado |
| GET    | `/current` | autenticado (404 si no hay evento en curso) |
| GET    | `/{id}` | autenticado |
| POST   | `/` | ADMIN, MANAGER |
| PUT    | `/{id}` | ADMIN, MANAGER |
| DELETE | `/{id}` | ADMIN (soft delete) |

### Sales (`/api/v1/sales`)

| Método | Ruta | Rol |
|---|---|---|
| POST   | `/` | autenticado (descuento atómico de stock) |
| GET    | `?eventId=&from=&to=&paymentMethod=&includeCancelled=&page=&size=` | autenticado |
| GET    | `/{id}` | autenticado |
| PATCH  | `/{id}/cancel` | ADMIN, MANAGER (idempotente, restituye stock) |
| GET    | `/stats?eventId=` | autenticado |

## Manejo de errores

Todas las respuestas de error siguen [RFC 7807 (`application/problem+json`)](https://www.rfc-editor.org/rfc/rfc7807):

```json
{
  "type": "about:blank",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "Producto con id 999 no encontrado",
  "instance": "/api/v1/products/999"
}
```

Mapeo de excepciones de dominio:

| Excepción | HTTP |
|---|---|
| `NotFoundException` | 404 |
| `ConflictException` | 409 |
| `ValidationException` | 422 |
| `BusinessRuleException` | 422 |
| Bean validation (`@Valid`) | 400 con array `errors` |
| Auth ausente / inválido | 401 |
| Falta de permisos | 403 |

## Build

```bash
./gradlew build
```

## Docker

Build local:

```bash
docker build -t perroamor/inventory-backend .
```

Run con perfil `prod` y env vars:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/inventory \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e APP_JWT_SECRET=... \
  -e APP_CORS_ALLOWED_ORIGINS=https://miapp.com \
  perroamor/inventory-backend
```

## Variables de entorno (perfil `prod`)

| Variable | Requerida | Default | Descripción |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | sí | `local` | Setear a `prod` en producción |
| `SPRING_DATASOURCE_URL` | sí | — | JDBC URL de Postgres |
| `SPRING_DATASOURCE_USERNAME` | sí | — | User de DB |
| `SPRING_DATASOURCE_PASSWORD` | sí | — | Password de DB |
| `APP_JWT_SECRET` | sí | — | Secret HS256 (mínimo 256 bits) |
| `APP_JWT_ACCESS_TTL_MIN` | no | 15 | TTL del access token (minutos) |
| `APP_JWT_REFRESH_TTL_DAYS` | no | 7 | TTL del refresh token (días) |
| `APP_CORS_ALLOWED_ORIGINS` | sí | — | Lista de origins separados por coma |
| `DB_POOL_MAX` | no | 20 | Pool max connections HikariCP |
| `DB_POOL_MIN` | no | 5 | Pool min idle HikariCP |

## Perfiles

- **`local`** — desarrollo contra Postgres en docker-compose. CORS abierto a `localhost:5173`. Health detallado siempre. Logging en DEBUG. (default)
- **`prod`** — producción. Credenciales y CORS desde env vars. Health detallado solo a usuarios autenticados. Logging en INFO.

## API versioning

Todas las rutas viven bajo `/api/v1`. Cualquier cambio breaking abre `/api/v2` y mantiene `/v1` deprecated por al menos un release antes de remover.

## Migraciones

Versionadas con Flyway en `src/main/resources/db/migration/`. La numeración sigue `V<n>__<descripcion>.sql`. Hibernate corre con `ddl-auto: validate` — si las entities y la DB divergen, la app no arranca.

## Testing

Los tests unitarios y de integración (Testcontainers) están planeados para fase 7 del plan.

## Documentación adicional

- [`docs/backend-plan.md`](docs/backend-plan.md) — plan canónico del rewrite con decisiones de arquitectura y fases.
- [`docs/spring-boot-4-notes.md`](docs/spring-boot-4-notes.md) — gotchas de Spring Boot 4 que encontramos durante el rewrite.
