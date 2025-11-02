# Use official Java runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy pom.xml and source
COPY pom.xml .
COPY src ./src

# Build the JAR file using Maven Wrapper (if available)
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Copy the final JAR to run
RUN cp target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
