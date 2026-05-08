# Notas de Spring Boot 4 — para tener en el radar

Hallazgos prácticos durante la build del backend Perro Amor (Spring Boot 4.0.5 + Java 25). Solo lo que **nos costó tiempo en el momento** y conviene saber antes de toparlo.

---

## 1. Auto-configs modularizadas por artifact

**Qué cambió**: en SB3, una sola dependencia (`spring-boot-autoconfigure`) traía todas las auto-configs (Hibernate, Flyway, Liquibase, Quartz, Mail, etc.). Cualquier librería en el classpath se activaba automáticamente. En SB4 cada auto-config vive en su **propio artifact** (`spring-boot-flyway`, `spring-boot-hibernate`, `spring-boot-quartz`, `spring-boot-mail`, …).

**Por qué importa**: tener `flyway-core` solo NO alcanza para que Spring Boot enchufe Flyway. Necesitás explícitamente la dep que trae la auto-config, además del driver y la lib core.

**Cómo lo manejamos** (`build.gradle.kts`):

```kotlin
implementation("org.springframework.boot:spring-boot-flyway")  // ← trae FlywayAutoConfiguration
implementation("org.flywaydb:flyway-database-postgresql")      // ← driver Postgres
```

**Pista para diagnosticarlo**: si una librería que esperás auto-configurada no arranca y los logs no muestran nada de ella (no aparece "Flyway Community Edition...", no aparece "Quartz Scheduler...", etc.), revisá si te falta el módulo `spring-boot-<x>` correspondiente.

---

## 2. Jackson 3 es el default — paquete `tools.jackson`

**Qué cambió**: SB4 migró el JSON default de **Jackson 2** a **Jackson 3**. Para evitar choques de classpath con código que aún use Jackson 2, los autores movieron el paquete raíz de `com.fasterxml.jackson` a **`tools.jackson`**. Es un cambio gigante a propósito.

**Por qué importa**: si inyectás `ObjectMapper` en un bean tuyo y lo importás desde `com.fasterxml.jackson.databind`, Spring no encuentra ningún bean de ese tipo. La auto-config registra solo el `ObjectMapper` de Jackson 3 (`tools.jackson.databind.ObjectMapper`). Falla con:
> No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper' available.

**Cómo lo manejamos**: en cualquier bean nuestro que necesite el ObjectMapper de Spring, importarlo del paquete nuevo:

```java
import tools.jackson.databind.ObjectMapper;  // ← Jackson 3 (default en SB4)
```

**Convivencia**: Jackson 2 sigue en el classpath porque librerías como `springdoc-openapi 2.x` lo usan internamente. Tu código no debería depender del Jackson 2 directamente.

---

## 3. Paquetes de auto-config se reorganizaron

**Qué cambió**: los auto-configs viven en paquetes que reflejan el módulo. Ejemplos vistos en stack traces:

| SB3 (típico) | SB4 |
|---|---|
| `org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration` | `org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration` |
| `org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration` | (queda en `spring-boot-flyway` con su propio paquete) |

**Por qué importa**: si tenés `@AutoConfigureBefore`/`@AutoConfigureAfter` o exclusiones de auto-config con paths del SB3 viejo, ya no apuntan a nada y el orden o exclusión se rompe silenciosamente.

**Cómo lo manejamos**: no hardcodeamos paths de auto-config. Usamos la convención por starter.

---

## 4. ProblemDetail (RFC 7807) por default — pero hay que activarlo

**Qué cambió**: Spring 6+ ya tenía `ProblemDetail`, pero el flag para que **todos** los errores no manejados devuelvan `application/problem+json` automáticamente sigue siendo opt-in.

**Por qué importa**: sin ese flag, un 404 de "ruta inexistente" devuelve el HTML del BasicErrorController en vez de un body JSON consistente.

**Cómo lo manejamos** (`application.yml`):

```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

**Cuidado**: el filtro de `oauth2-resource-server` no pasa por BasicErrorController para sus 401, así que ese flag NO los convierte a `ProblemDetail`. Para emitir 401 con shape consistente necesitás un `AuthenticationEntryPoint` custom que serialice un `ProblemDetail` a mano (lo hicimos en `ProblemDetailAuthenticationEntryPoint`).

---

## 5. Virtual threads activables con un flag

**Qué cambió**: ya no necesitás configurar Tomcat a mano para usar virtual threads. SB3.2+ y SB4 lo activan globalmente con una property.

**Cómo lo manejamos** (`application.yml`):

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Efecto**: Tomcat usa virtual threads para handlers HTTP, `@Async` los usa, scheduler los usa. Java 21+ requerido (acá Java 25).

**Cuidado**: hay librerías que usan `synchronized` blocks pesados (algunas DB drivers viejos, Drivers JDBC pre-21). Eso "pinea" virtual threads a carrier threads y mata el beneficio. Postgres JDBC 42.x está bien.

---

## 6. springdoc-openapi sigue en Jackson 2

**Qué cambió**: nada en springdoc, pero como SB4 usa Jackson 3 por default, springdoc 2.x **arrastra Jackson 2 al classpath** porque internamente lo usa.

**Por qué importa**: cuando hagas un `./gradlew dependencies` vas a ver dos versiones de jackson-databind:

```
+--- com.fasterxml.jackson.core:jackson-databind:2.21.2  (Jackson 2 — usado por springdoc)
+--- tools.jackson.core:jackson-databind:3.1.0           (Jackson 3 — default de SB4)
```

No es un bug ni un conflicto — son paquetes distintos, conviven sin choque. Pero tu código de aplicación debe usar Jackson 3 (`tools.jackson`), no el viejo.

---

## 7. `spring-boot-configuration-processor` para `@ConfigurationProperties` custom

**Qué cambió**: nada nuevo, pero la práctica de usar **records** para `@ConfigurationProperties` (que es lo que conviene en Java 21+) hace que el IDE no encuentre metadata sin el processor.

**Por qué importa**: el IDE marca warnings tipo "Cannot resolve configuration property 'app.cors.allowed-origins'" en `application.yml`, y no autocompleta. No es bug runtime, es DX.

**Cómo lo manejamos** (`build.gradle.kts`):

```kotlin
annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
```

Genera `META-INF/spring-configuration-metadata.json` automático para tus records con `@ConfigurationProperties`.

---

## 8. CORS preflight: el header `Access-Control-Allow-Origin` solo aparece si hay handler

**Qué cambió**: nada (es comportamiento histórico de Spring MVC), pero es trampa común.

**Por qué importa**: si hacés un preflight `OPTIONS` contra una URL que no tiene mapping, Spring devuelve 404 con headers `Vary: Origin, ...` pero **sin** `Access-Control-Allow-Origin`. No es bug — el filtro CORS solo emite el header cuando un handler matchea.

**Cómo verificarlo**: el preflight contra un endpoint que SÍ existe (en nuestro caso, una vez que tengamos `/api/v1/auth/login` real, fase 2). No se puede testear CORS con URLs vacías.

---

## 9. Hibernate 7.x trae cambios sutiles

**Qué cambió**: SB4 trae Hibernate ORM 7.2.x. Algunas APIs deprecated en Hibernate 6 se removieron.

**Por qué importa**: si copiás snippets de stack overflow con `org.hibernate.SessionFactory.openSession()` patterns viejos o anotaciones legacy de Hibernate (`@Type`, `@TypeDef` antiguos), pueden no compilar o no funcionar.

**Cómo lo manejamos**: por ahora, JPA estándar (Jakarta Persistence 3.2) sin APIs Hibernate-specific. Si más adelante necesitamos algo nativo de Hibernate, revisamos contra Hibernate 7.x docs, no 5.x/6.x.

---

## 10. NimbusJwtEncoder con HS256 requiere `JwsHeader` explícito

**Qué cambió**: nada — es comportamiento histórico de Spring Security oauth2-jose, pero es contraintuitivo.

**Por qué importa**: si emitís un JWT con `encoder.encode(JwtEncoderParameters.from(claims))` (sin pasar header), NimbusJwtEncoder asume **RS256 (RSA) por default** al elegir la clave del JWK set. Si tu setup es HMAC simétrico (HS256), no matchea ninguna key y tira:

> JwtEncodingException: Failed to select a JWK signing key

**Cómo lo manejamos** (`JwtService`):

```java
private static final JwsHeader HS256_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

// y al codificar:
encoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims))
```

El header le dice al encoder qué algoritmo usar y matchea la `OctetSequenceKey` que registramos en el JWK set.

**Regla mental**: si registrás keys con `algorithm(JWSAlgorithm.HS256)`, codificá siempre pasando un `JwsHeader.with(MacAlgorithm.HS256)`. Lo mismo si usás RS512 o cualquier otro — el header tiene que matchear.

---

## Tabla resumen — para repasar antes de dormir

| Tema | Trampa | Solución de 1 línea |
|---|---|---|
| Auto-configs | Falta el módulo `spring-boot-<x>` | Agregarlo además del lib core |
| Jackson | `com.fasterxml.jackson` no se inyecta | Usar `tools.jackson.databind.ObjectMapper` |
| ProblemDetail | 404 devuelve HTML | `spring.mvc.problemdetails.enabled=true` |
| 401 ProblemDetail | Auth entry point devuelve body vacío | `AuthenticationEntryPoint` custom |
| Virtual threads | Off por default | `spring.threads.virtual.enabled=true` |
| Jackson 2 en classpath | Sale de `springdoc` | Ignorar — convivencia esperada |
| `@ConfigurationProperties` | IDE no autocompleta | `spring-boot-configuration-processor` annotation processor |
| CORS preflight | No emite `Allow-Origin` en 404 | Probar contra endpoint real |
| Hibernate APIs | Deprecated removidas | Mantenerse en JPA estándar |
| JwtEncoder HS256 | "Failed to select a JWK signing key" | `JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)` |
