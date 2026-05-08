FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
RUN addgroup --system --gid 1001 spring && adduser --system --uid 1001 --ingroup spring spring
WORKDIR /app
COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
