# Use official OpenJDK 21 image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the built jar (adjust the name if needed)
COPY target/intelsync-ingestion-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]