#Build stage

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY lib ./lib
COPY src ./src

RUN mvn clean package -DskipTests


#Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/brasfoot-save-editor-2.0.0-SNAPSHOT.jar app.jar
COPY lib ./lib

CMD ["java", "-cp", "app.jar:lib/*", "org.springframework.boot.loader.launch.JarLauncher"]

