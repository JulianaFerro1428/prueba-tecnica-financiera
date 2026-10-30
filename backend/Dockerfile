# Etapa 1: compila el .jar. Trae Maven y el JDK, así que no hace falta tenerlos instalados en la máquina.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom.xml: si no cambian las dependencias, Docker reutiliza esta capa y no las descarga de nuevo.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
# Las pruebas se ejecutan aparte con "mvn test": las de integración necesitan Docker (Testcontainers),
# que no está disponible dentro de esta construcción.
RUN mvn -q package -DskipTests

# Etapa 2: imagen final pequeña, solo con el JRE y el .jar.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# La aplicación no corre como root dentro del contenedor.
RUN addgroup -S bank && adduser -S bank -G bank
COPY --from=build /app/target/*.jar app.jar
USER bank

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
