# Perro Amor — Inventory Backend

Backend del sistema de inventario y POS de Perro Amor.

## Stack

- Java 25 (LTS)
- Spring Boot 4.0.5
- Gradle Kotlin DSL (wrapper 9.4.1)
- PostgreSQL 15
- Flyway (migraciones)
- MapStruct (mapeo dominio ↔ JPA)
- springdoc-openapi (Swagger UI)

## Levantar local

### 1. Postgres

```bash
docker compose up -d
```

Esto deja Postgres 15 corriendo en `localhost:5432` con DB `inventory`, user `inventory`, password `inventory`.

### 2. Aplicación

```bash
./gradlew bootRun
```

La app levanta en `http://localhost:8080` con perfil `local` por default.

### 3. Verificar

- Health: <http://localhost:8080/actuator/health>
- Swagger UI: <http://localhost:8080/swagger-ui.html> (cuando se exponga en fase 1)

## Build

```bash
./gradlew build
```

## Estructura

```
src/main/java/com/perroamor/inventory/
├── InventoryApplication.java
├── shared/      # cross-cutting (errores, tipos, config)
├── auth/        # autenticación (users, roles, JWT)
├── catalog/     # marcas, productos, variantes
├── events/      # eventos
└── sales/       # ventas (POS)
```

Cada bounded context se separa en `domain/`, `application/` e `infrastructure/`.
Reglas de capa y convenciones: ver `docs/backend-plan.md`.

## Perfiles

- `local` — desarrollo contra Postgres en docker-compose (default)
- `prod` — producción, lee credenciales de env vars

## Docker

Build y run de la imagen:

```bash
docker build -t perroamor/inventory-backend .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/inventory \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  perroamor/inventory-backend
```
