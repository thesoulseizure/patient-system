# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Build the project (download dependencies)
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application (skip tests to speed up build)
RUN ./mvnw clean package -DskipTests

# Expose the port your app runs on (Render defaults to 8080)
EXPOSE 8080

# Set environment variables for memory management
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the Spring Boot application
CMD ["java", "-jar", "target/patient-system-1.0-SNAPSHOT.jar"]
