# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first for Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY src ./src

# Build the Spring Boot JAR
RUN mvn clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]