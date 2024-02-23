# Use the official OpenJDK image as the base image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Java application JAR file into the container at /app
COPY target/LMS-0.0.1-SNAPSHOT.jar /app

# Expose the port your application runs on
EXPOSE 8002

# Command to run the application when the container starts
CMD ["java", "-jar", "LMS-0.0.1-SNAPSHOT.jar"]
