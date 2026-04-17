#Build stage

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY lib ./lib
COPY src ./src

RUN mvn install:install-file -Dfile=lib/brasfoot.jar -DgroupId=com.brasfoot -DartifactId=brasfoot-game -Dversion=1.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=lib/asm-5.1-es.jar -DgroupId=org.ow2.asm -DartifactId=asm -Dversion=5.1-es -Dpackaging=jar
RUN mvn install:install-file -Dfile=lib/reflectasm-1.11.5.jar -DgroupId=com.esotericsoftware -DartifactId=reflectasm -Dversion=1.11.5 -Dpackaging=jar

RUN mvn clean package -DskipTests


#Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

