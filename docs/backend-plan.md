# Backend Rewrite Plan — Perro Amor Inventory System

> **Audiencia**: una sesión de Claude Code que ejecuta este plan en una carpeta vacía, en colaboración con el usuario (Erick).
> **Origen del plan**: sesión de planificación previa con visibilidad completa de la app actual (Vaadin + Kotlin) en `/Users/erick.quintanar/Documents/personal/repositories/inventory-system`.
> **Reglas de ejecución**: una fase por vez. Al terminar cada fase, parar y pedir validación al usuario antes de pasar a la siguiente. Conventional commits, sin firmas Co-Authored-By.

---

## 0. Contexto

### Por qué este rewrite existe
La app actual es un monolito Vaadin (server-rendered) que el usuario ya no quiere mantener: la UI se siente lenta porque cada interacción hace round-trip al server, y la idea es convertir esto a un backoffice más amplio. Plan: separar en backend (esta carpeta) + frontend SPA (otra carpeta, otro plan).

**La app vieja queda viva en producción.** No la tocamos. Solo se consulta para entender el dominio.

### Referencia a la app actual (read-only)
- Path: `/Users/erick.quintanar/Documents/personal/repositories/inventory-system`
- Carpetas clave para consultar:
  - `src/main/kotlin/com/perroamor/inventory/entity/` — modelos de dominio
  - `src/main/kotlin/com/perroamor/inventory/service/` — reglas de negocio
  - `src/main/resources/db/migration/` — schema actual (V1–V8)
- Reglas: **leer, NO escribir** en esa carpeta.

### MVP scope (FASES 0–6 de este plan)
Solo el slice del POS:
- Auth con JWT
- Marcas, productos, variantes (catálogo)
- Eventos
- Ventas (POS) + listado con stats básicas

**Fuera del MVP** (NO implementar salvo que el usuario lo pida explícito):
- Reportes avanzados / dashboards
- UI de gestión de usuarios (los usuarios se crean por seed o endpoint admin)
- Backoffice extendido

---

## 1. Decisiones de arquitectura (locked)

| Decisión | Valor | Razón |
|---|---|---|
| Lenguaje | Java 25 (LTS) | Stack principal del usuario, modernísima |
| Framework | Spring Boot 4.0.5 | Última estable, soporte first-class de Java 25 |
| Build | Gradle Kotlin DSL | Estándar moderno, mismo build script style que la app vieja |
| DB | PostgreSQL 15+ | Mismo motor que producción actual |
| Migraciones | Flyway | Ya validado en la app actual |
| Auth | JWT stateless | Encaja con SPA, no necesita session storage |
| Mapeo | MapStruct | Domain ↔ JPA sin escribir mappers a mano |
| Validación | Jakarta Bean Validation | Estándar |
| API docs | springdoc-openapi (Swagger UI) | Auto-genera contrato para el frontend |
| Arquitectura | Layered limpia (`domain` / `application` / `infrastructure`) | Hexagonal sería overkill para REST + JPA |
| Tests | JUnit 5 + Testcontainers | Última fase, opcional |

**Java features que conviene usar**: records, sealed types, pattern matching, virtual threads (Spring Boot 4 los usa por default si activás `spring.threads.virtual.enabled=true`).

**No usar**:
- Lombok (Java 25 con records hace innecesario el 90% de Lombok).
- Hibernate-specific APIs en el dominio (mantener el dominio puro, sin anotaciones JPA).
- DTOs de respuesta hardcodeados — usar records.

---

## 2. Estructura de carpetas

El package raíz: `com.perroamor.inventory`. Dentro:

```
src/main/java/com/perroamor/inventory/
├── InventoryApplication.java
├── shared/                          # cross-cutting (tipos compartidos, errores)
│   ├── error/                       # excepciones de dominio + ProblemDetail handler
│   └── types/                       # value objects compartidos (Money, etc.)
├── auth/                            # bounded context: autenticación
│   ├── domain/                      # User, Role (dominio puro)
│   ├── application/                 # AuthService, casos de uso
│   ├── infrastructure/
│   │   ├── persistence/             # UserJpaEntity, mappers, repos JPA
│   │   ├── security/                # JWT, filtros, SecurityConfig
│   │   └── web/                     # AuthController, DTOs
├── catalog/                         # bounded context: catálogo
│   ├── domain/                      # Brand, Product, ProductVariant
│   ├── application/                 # BrandService, ProductService, etc.
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── web/                     # BrandController, ProductController
├── events/                          # bounded context: eventos
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── sales/                           # bounded context: ventas (POS)
│   ├── domain/                      # Sale, SaleItem, value objects
│   ├── application/                 # SaleService (descuento de stock, totales)
│   └── infrastructure/
│       ├── persistence/
│       └── web/

src/main/resources/
├── application.yml                  # config base
├── application-local.yml            # dev (Postgres en docker-compose)
├── application-prod.yml             # producción
└── db/migration/                    # V1__init_schema.sql, V2__seed_admin.sql, ...

src/test/java/com/perroamor/inventory/
└── (espejo de la estructura main)
```

### Reglas de capa
- `domain/` no importa nada de Spring, JPA, Jackson, ni de otros bounded contexts. Solo Java puro.
- `application/` puede depender de `domain/` y de puertos definidos en `domain/`. NO importa JPA ni anotaciones web.
- `infrastructure/` implementa los puertos + expone HTTP. Depende de Spring/JPA.
- Bounded contexts NO se importan entre sí en `domain/` ni `application/`. Si necesitan comunicarse, lo hacen vía:
  - Un service en `application/` que recibe IDs y orquesta (preferido para el MVP).
  - Eventos de dominio (más adelante, no en MVP).

### Convenciones de naming
- Entidades JPA: sufijo `JpaEntity` (ej: `ProductJpaEntity`).
- Repositorios JPA: sufijo `JpaRepository` (ej: `ProductJpaRepository`).
- Puertos del dominio: `XxxRepository` (interface en `domain/`), implementado en `infrastructure/persistence/`.
- DTOs de request: sufijo `Request` (records).
- DTOs de response: sufijo `Response` (records).
- Mappers MapStruct: sufijo `Mapper` (ej: `ProductMapper`).

---

## 3. FASES

**Importante**: cada fase termina con un commit limpio y verificación con el usuario antes de avanzar. Si algo no compila, no se cierra la fase.

---

### FASE 0 — Bootstrap del proyecto

**Objetivo**: tener un Spring Boot 4.0.5 que arranca, conecta a Postgres local, expone `/actuator/health` y compila sin warnings.

**Pasos**:
1. Confirmar con el usuario el path de la nueva carpeta (debe estar vacía o solo tener `.git`).
2. Generar proyecto base con start.spring.io o manual:
   - Group: `com.perroamor`
   - Artifact: `inventory-backend`
   - Java 25
   - Gradle Kotlin DSL
   - Spring Boot 4.0.5
   - Dependencies iniciales: `web`, `data-jpa`, `security`, `validation`, `actuator`, `flyway`, `flyway-database-postgresql`, `postgresql` (runtime), `springdoc-openapi-starter-webmvc-ui`
3. Agregar dependencia de MapStruct + processor en `build.gradle.kts`.
4. Crear estructura de carpetas según sección 2 (vacías o con un `.gitkeep`).
5. `application.yml` con perfil base; `application-local.yml` con datasource a Postgres local.
6. `docker-compose.yml` para Postgres 15 local (mismo formato que la app vieja).
7. `Dockerfile` multi-stage (build con `eclipse-temurin:25-jdk` + runtime `25-jre`).
8. `.gitignore` (build, .idea, .gradle, etc.).
9. `README.md` mínimo con cómo levantar local.
10. Verificar: `./gradlew bootRun`, hit a `/actuator/health` devuelve 200.

**Done cuando**:
- [x] La app arranca contra Postgres local sin error.
- [x] `/actuator/health` responde `{"status":"UP"}`.
- [x] `./gradlew build` pasa.
- [x] Commit: `chore: bootstrap spring boot 4.0.5 + java 25 project`.

---

### FASE 1 — Cross-cutting (errores, OpenAPI, CORS, perfiles)

**Objetivo**: dejar listas las piezas transversales que TODA fase siguiente va a consumir.

**Pasos**:
1. **Manejo de errores**:
   - Crear `shared/error/DomainException.java` (sealed) con subclases: `NotFoundException`, `ConflictException`, `ValidationException`, `BusinessRuleException`.
   - Crear `shared/error/GlobalExceptionHandler` (`@RestControllerAdvice`) que mapea cada excepción a `ProblemDetail` (RFC 7807).
2. **OpenAPI**:
   - Bean de configuración en `shared/config/OpenApiConfig.java` con título, versión, descripción.
   - Verificar que `/swagger-ui.html` carga.
3. **CORS**:
   - Bean de `WebMvcConfigurer` que permite el origin del frontend (configurable por property `app.cors.allowed-origins`).
   - En `local`: `http://localhost:5173` (Vite default).
4. **Virtual threads**:
   - `application.yml`: `spring.threads.virtual.enabled=true`.
5. **Logging**:
   - `application.yml`: nivel root INFO, `com.perroamor` en DEBUG en local, INFO en prod.
   - Formato console con timestamp, thread, logger, mensaje.

**Done cuando**:
- [x] Hit a una ruta inexistente devuelve un `ProblemDetail` con shape consistente.
- [x] `/swagger-ui.html` carga (sin endpoints todavía, pero la UI está).
- [x] CORS funciona con el origin configurado.
- [x] Commit: `feat: cross-cutting (error handling, openapi, cors, profiles)`.

---

### FASE 2 — Auth (Users, Roles, JWT)

**Objetivo**: login funcional con JWT, endpoint `/me`, autorización por rol.

**Modelo de dominio**:
- `Role`: id, name (`ADMIN` | `MANAGER` | `EMPLOYEE`), description, isActive.
- `User`: id, username, passwordHash, email, fullName, roleId, isActive, createdAt, lastLogin.

**Endpoints**:
- `POST /api/v1/auth/login` → `{username, password}` → `{accessToken, refreshToken, expiresIn, user: {...}}`
- `POST /api/v1/auth/refresh` → `{refreshToken}` → `{accessToken, refreshToken, expiresIn}`
- `GET /api/v1/auth/me` → datos del usuario autenticado
- `POST /api/v1/auth/logout` → invalida refresh token (opcional, simple en MVP: el cliente borra el token)

**Pasos**:
1. **Domain**: records `Role`, `User` en `auth/domain/`. Puerto `UserRepository`, `RoleRepository`.
2. **Persistence**:
   - `RoleJpaEntity`, `UserJpaEntity` con sus mappers MapStruct.
   - Implementaciones de los puertos.
3. **Migraciones Flyway**:
   - `V1__init_auth_schema.sql`: tablas `roles` y `users`.
   - `V2__seed_roles_and_admin.sql`: inserta los 3 roles + un admin con password hardcodeado (BCrypt) que el usuario va a cambiar después.
4. **Security**:
   - Spring Security 7 (viene en Spring Boot 4) con JWT.
   - Filtro JWT que valida Bearer token y carga `UserDetails`.
   - `BCryptPasswordEncoder`.
   - `SecurityConfig` con: `/api/v1/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health` públicos. Resto autenticado.
   - Properties: `app.jwt.secret`, `app.jwt.access-ttl-minutes` (15), `app.jwt.refresh-ttl-days` (7).
5. **Application service**: `AuthService` con `login`, `refresh`, `getCurrentUser`.
6. **Web**: `AuthController` con los endpoints, request/response records, validation.

**Done cuando**:
- [ ] Login con credenciales seedeadas devuelve JWT válido.
- [ ] Hit a `/me` con `Authorization: Bearer <token>` devuelve usuario.
- [ ] Hit sin token devuelve 401 con `ProblemDetail`.
- [ ] Hit con token inválido devuelve 401.
- [ ] Refresh funciona.
- [ ] Commit: `feat(auth): jwt authentication with users and roles`.

---

### FASE 3 — Catalog (Brands + Products + Variants)

**Objetivo**: CRUD completo del catálogo, listo para que la UI muestre y edite.

**Modelo de dominio**:

`Brand`:
- id, name, description, isActive, createdAt.

`Product`:
- id, name, brandId, category, price (BigDecimal), wholesalePrice (BigDecimal), stock (int), description, canBePersonalized (bool), hasVariants (bool).
- Reglas: si `hasVariants=true`, el stock real vive en las variantes; el campo `stock` del producto representa total agregado.

`ProductVariant`:
- id, productId, variantName, color, size, design, material, sku (único), stock, priceAdjustment (BigDecimal, puede ser 0), isActive.
- Reglas: el precio efectivo de la variante = `product.price + priceAdjustment`.

**Endpoints (todos requieren auth, mayoría requieren MANAGER o ADMIN salvo lectura)**:

Brands:
- `GET /api/v1/brands` (any authenticated)
- `GET /api/v1/brands/{id}`
- `POST /api/v1/brands` (ADMIN/MANAGER)
- `PUT /api/v1/brands/{id}` (ADMIN/MANAGER)
- `DELETE /api/v1/brands/{id}` (ADMIN, soft delete)

Products:
- `GET /api/v1/products?brandId=&category=&q=&page=&size=` (paginated)
- `GET /api/v1/products/{id}`
- `POST /api/v1/products` (ADMIN/MANAGER)
- `PUT /api/v1/products/{id}` (ADMIN/MANAGER)
- `DELETE /api/v1/products/{id}` (ADMIN, soft delete)
- `PATCH /api/v1/products/{id}/stock` (ADMIN/MANAGER) — ajuste manual de stock

Variants:
- `GET /api/v1/products/{productId}/variants`
- `GET /api/v1/variants/{id}`
- `POST /api/v1/products/{productId}/variants` (ADMIN/MANAGER)
- `PUT /api/v1/variants/{id}` (ADMIN/MANAGER)
- `DELETE /api/v1/variants/{id}` (ADMIN, soft delete)

**Pasos**:
1. Domain: records + puertos.
2. Persistence: JPA entities + mappers + repos.
3. Migraciones Flyway:
   - `V3__catalog_schema.sql` (tablas brands, products, product_variants + índices).
   - `V4__seed_brands.sql` (Perro Amor + Perra Madre).
   - `V5__seed_products_perro_amor.sql` (los 7 productos actuales con sus variantes).
   - `V6__seed_products_perra_madre.sql` (los 3 actuales).
4. Application services con validaciones de dominio (no permitir SKU duplicado, etc.).
5. Web: controllers + DTOs + validación.
6. Pagination con `Pageable` de Spring Data.

**Done cuando**:
- [ ] CRUD completo de brands, products, variants funciona vía Swagger UI.
- [ ] Filtros de productos (`brandId`, `category`, `q`) andan.
- [ ] Validaciones devuelven `ProblemDetail` claros.
- [ ] Productos seedeados aparecen al hacer GET sin filtros.
- [ ] Commit: `feat(catalog): brands, products and variants management`.

---

### FASE 4 — Events

**Objetivo**: CRUD de eventos con estado derivado.

**Modelo de dominio**:
- `Event`: id, name, location, description, startDate, endDate, isActive.
- `EventStatus` (enum derivado, no persistido): `UPCOMING` | `IN_PROGRESS` | `FINISHED`.
- Regla: estado se calcula de `startDate`/`endDate` vs `LocalDate.now()`.

**Endpoints**:
- `GET /api/v1/events?status=&page=&size=` (any authenticated)
- `GET /api/v1/events/current` → evento en curso (o 404)
- `GET /api/v1/events/{id}`
- `POST /api/v1/events` (ADMIN/MANAGER)
- `PUT /api/v1/events/{id}` (ADMIN/MANAGER)
- `DELETE /api/v1/events/{id}` (ADMIN, soft delete)

**Pasos**:
1. Domain + puerto.
2. Persistence + mapper.
3. `V7__events_schema.sql`.
4. Application service: `getCurrentEvent` busca por rango de fechas.
5. Web: controller con DTO que incluye el `status` calculado.

**Done cuando**:
- [ ] Crear evento, listarlo, ver el current.
- [ ] El status calculado coincide con la fecha actual.
- [ ] Commit: `feat(events): event management with derived status`.

---

### FASE 5 — Sales (POS — corazón del MVP)

**Objetivo**: registrar ventas con descuento de stock atómico, calcular totales, listar y cancelar.

Esta fase es la más delicada. Ojo con la consistencia.

**Modelo de dominio**:

`Sale`:
- id, eventId, saleDate (LocalDateTime), totalAmount, paymentMethod (`CASH` | `CARD` | `TRANSFER` | `OTHER`), customerName?, customerPhone?, customerEmail?, notes?, discountAmount, taxAmount, isPaid, isCancelled, soldByUserId (FK a users).
- Calculados: `subtotal = totalAmount - taxAmount + discountAmount` (mismo método que la app vieja).

`SaleItem`:
- id, saleId, productId, variantId? (nullable), quantity, unitPrice (BigDecimal — snapshot al momento de la venta), personalization? (string), lineTotal (calculado).

**Reglas de negocio críticas**:
1. Una venta solo puede crearse contra un evento `IN_PROGRESS` (con flag para forzar testing offline si hace falta — pero NO en MVP).
2. Crear una venta descuenta stock atómicamente:
   - Si el item tiene `variantId`, descuenta de la variante.
   - Si no, descuenta del producto.
   - Si no hay stock suficiente → `BusinessRuleException` y rollback total.
3. Cancelar una venta REVIERTE el stock.
4. El `unitPrice` se SNAPSHOTEA al crear la venta — si el precio del producto cambia después, las ventas históricas no se afectan.
5. `totalAmount = sum(item.lineTotal) - discountAmount + taxAmount`.

**Endpoints**:
- `POST /api/v1/sales` (any authenticated) — crear venta
- `GET /api/v1/sales?eventId=&from=&to=&paymentMethod=&page=&size=` — listar (any authenticated)
- `GET /api/v1/sales/{id}` (any authenticated)
- `PATCH /api/v1/sales/{id}/cancel` (ADMIN/MANAGER) — cancelar
- `GET /api/v1/sales/stats?eventId=` — stats por evento (total, breakdown por payment method, count)

**Request shape de POST /sales**:
```json
{
  "eventId": 1,
  "paymentMethod": "CASH",
  "customerName": "...",
  "customerPhone": "...",
  "discountAmount": 0,
  "taxAmount": 0,
  "notes": "...",
  "items": [
    {
      "productId": 12,
      "variantId": 45,
      "quantity": 2,
      "unitPrice": 199.00,
      "personalization": "Firulais"
    }
  ]
}
```

**Pasos**:
1. Domain con records + puertos.
2. Persistence: `SaleJpaEntity`, `SaleItemJpaEntity` (relación 1:N con cascade ALL).
3. `V8__sales_schema.sql` con FK a events, products, variants, users.
4. `SaleService.createSale()`:
   - `@Transactional` (Propagation.REQUIRED).
   - Valida evento en curso.
   - Valida y descuenta stock con bloqueo pesimista (`SELECT ... FOR UPDATE`) o use `@Version` optimistic en Product/Variant — en MVP, optimistic con retry simple basta.
   - Snapshotea precios.
   - Calcula totales.
   - Persiste todo.
5. `SaleService.cancelSale()`:
   - `@Transactional`.
   - Revierte stock.
   - Marca `isCancelled=true`.
   - Idempotente: si ya está cancelada, devuelve sin error.
6. Endpoint de stats: agregaciones SQL nativas o via JPQL con groupBy en `paymentMethod`.

**Done cuando**:
- [ ] Crear venta descuenta stock correctamente (verificar manualmente vía Swagger).
- [ ] Crear venta sin stock suficiente devuelve 422 con `ProblemDetail` + nada se persiste.
- [ ] Cancelar venta restituye stock.
- [ ] Stats endpoint devuelve totales y breakdown coherentes.
- [ ] Commit: `feat(sales): pos with atomic stock decrement and stats`.

---

### FASE 6 — Hardening (último step de MVP funcional)

**Objetivo**: dejar la API lista para que el frontend la consuma sin sorpresas en producción.

**Pasos**:
1. **Pagination consistente**: response wrapper `PagedResponse<T>` con `content`, `page`, `size`, `totalElements`, `totalPages`.
2. **API versioning**: ya está en `/api/v1`. Documentar en README que cualquier cambio breaking abre `/api/v2`.
3. **Rate limiting básico** (opcional, solo si el deploy lo exige): bucket por IP en `/auth/login` para no permitir brute force. Bucket4j si hace falta.
4. **Auditoría mínima**: campos `createdAt`, `updatedAt` automáticos via `@EntityListeners(AuditingEntityListener.class)` + `@EnableJpaAuditing`.
5. **Health detallado**: `/actuator/health` con DB check (ya viene gratis con `data-jpa`).
6. **Profile prod**: `application-prod.yml` que toma DB credentials de env vars (`SPRING_DATASOURCE_URL`, etc.). NO hardcodear como en la app vieja.
7. **Dockerfile final**: optimizado con JVM flags para containers (`-XX:+UseContainerSupport`, virtual threads habilitados).
8. **README expandido**: cómo correr local, cómo hacer deploy, lista de endpoints.

**Done cuando**:
- [ ] Listados paginados devuelven shape `PagedResponse<T>`.
- [ ] App levanta con perfil `prod` cuando se le pasan env vars.
- [ ] Imagen Docker arranca y responde health.
- [ ] Commit: `feat: hardening (pagination, auditing, prod config)`.

---

### FASE 7 — Tests (opcional, post-MVP)

**Objetivo**: red de seguridad mínima sobre la lógica que mueve plata.

**Prioridades** (de arriba a abajo, parar cuando el usuario diga "alcanza"):

1. **Unit tests de domain** (no necesitan Spring):
   - Cálculo de totales en `Sale`.
   - Estado derivado de `Event`.
   - Reglas de stock en `Product` y `ProductVariant`.
2. **Tests de application services con mocks** (puertos mockeados):
   - `SaleService.createSale` happy path + error path (sin stock).
   - `SaleService.cancelSale` idempotencia.
3. **Integration tests con Testcontainers**:
   - Postgres real, Flyway corriendo, transacciones reales.
   - Concurrencia en descuento de stock.
4. **API tests con MockMvc**:
   - Login flow.
   - 401 sin token / 403 con rol incorrecto.
   - CRUD básico de catalog.

**Done cuando**:
- [ ] El usuario decide que está bien.
- [ ] Commit: `test: domain and service coverage for sales-critical logic`.

---

### FASE 8 — Deployment (cuando el usuario lo pida)

Esto NO se ejecuta automáticamente. Queda como guía para cuando el usuario quiera deployar.

**Pasos** (a coordinar con el usuario):
1. Decidir plataforma (Railway, Fly.io, Render, AWS, etc.).
2. Configurar variables de entorno (DB url, JWT secret, CORS origins).
3. CI/CD: GitHub Actions con build + push de imagen Docker.
4. Healthcheck path: `/actuator/health`.
5. Plan de migración de datos:
   - Las migraciones Flyway de este nuevo backend NO son las mismas que la app vieja.
   - Hay que decidir: arrancamos con base limpia (mejor para MVP), o dump/restore desde la vieja.

---

## 4. Convenciones operativas

### Commits
- Conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`.
- Sin firmas Co-Authored-By.
- Un commit por fase mínimo. Subdividir si la fase es grande.
- Un push manual, lo decide el usuario.

### Branching
- `master` o `main` como default.
- Feature branches opcionales. Para este rewrite con sesiones guiadas, trabajar directo en main es aceptable porque cada fase se valida antes de mergear cambios.

### Engram (memoria persistente)
Si la sesión ejecutora tiene engram disponible:
- Al inicio: `mem_search` con keywords del proyecto para recuperar contexto previo.
- Al cerrar cada fase: `mem_save` con la fase, decisiones, y archivos tocados.
- Al cierre de sesión: `mem_session_summary`.

### Cuándo parar y consultar al usuario
- Antes de instalar dependencias nuevas no listadas en este plan.
- Antes de tomar decisiones de schema que se desvíen del modelo descrito.
- Si una validación falla y hay más de una forma de resolverla.
- SIEMPRE entre fases.

### Idioma
- Comentarios y docs internos: español, neutro/voseo.
- Identificadores (clases, métodos, variables): inglés.
- Strings de error visibles a usuarios: español (los va a consumir el frontend).

---

## 5. Lo que ESTE plan NO cubre (intencionalmente)

- Frontend: tiene su propio plan en `doc/frontend-plan.md` (cuando se escriba).
- Migración de datos productivos desde la app vieja.
- Reportes/dashboards complejos.
- Notificaciones (email, push).
- Multi-tenant.
- i18n (todo en español por ahora).
- WebSockets / real-time.

Si en algún momento durante la ejecución aparece una de estas, parar y consultar al usuario antes de incluirlas.

---

## 6. Checklist final del MVP

Cuando todas estas estén ✅, el MVP está terminado:

- [x] Fase 0: bootstrap
- [x] Fase 1: cross-cutting
- [ ] Fase 2: auth con JWT
- [ ] Fase 3: catálogo (brands, products, variants)
- [ ] Fase 4: events
- [ ] Fase 5: sales (POS)
- [ ] Fase 6: hardening
- [ ] (opcional) Fase 7: tests
- [ ] (cuando el usuario quiera) Fase 8: deployment

Después del MVP, el frontend se construye con su propio plan, consumiendo este backend.
