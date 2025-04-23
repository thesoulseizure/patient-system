# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port your app runs on
EXPOSE 8080

# Set environment variables for memory management
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the Spring Boot application
CMD ["java", "-jar", "target/patient-system-1.0-SNAPSHOT.jar"]
