# ---- Stage 1: Build the JAR ----
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the full source and build the app
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/SpringRestDemo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port expected by Render (optional but good practice)
EXPOSE 8080

# Set environment variables for Spring Boot
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=${PORT}

# Run the application using the dynamic port and profile
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

