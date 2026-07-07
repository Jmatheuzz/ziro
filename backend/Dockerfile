# --- Etapa de build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copia so o pom primeiro pra aproveitar cache de dependencias entre builds
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa final (imagem enxuta, so com o jar) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S ziro && adduser -S ziro -G ziro
COPY --from=build /app/target/*.jar app.jar
RUN chown ziro:ziro app.jar
USER ziro

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
