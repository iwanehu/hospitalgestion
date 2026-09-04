# ==========================================
# Build stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw dependency:go-offline -DskipTests

COPY src/ src/

RUN ./mvnw clean package -DskipTests


# ==========================================
# Runtime stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S hospital \
    && adduser -S hospital -G hospital

COPY --from=builder \
    /workspace/target/gestion-*.jar \
    /app/gestion.jar

RUN chown hospital:hospital /app/gestion.jar

USER hospital:hospital

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/gestion.jar"]
