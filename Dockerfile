# Build stage using Maven and JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Pre-download dependencies to leverage Docker layer caching
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage using a lightweight JRE 17 image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8099
ENTRYPOINT ["java", "-jar", "app.jar"]
