# Start from a base Java image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file
COPY target/SpringRestDemo-0.0.1-SNAPSHOT.jar app.jar

# Run the app
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose the port for Render (Render provides the PORT env variable)
EXPOSE 8080

# Use the PORT provided by Render and set active profile
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=${PORT}

# Run the application
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

