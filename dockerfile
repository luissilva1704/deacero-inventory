
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src

RUN mvn -q clean package -DskipTests



FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/inventario-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""

EXPOSE 8080


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
