# === Stage 1: Build the JAR using Maven ===
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set working directory inside container
WORKDIR /workspace

# Copy Maven descriptor and download dependencies first (cache-friendly)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy full source and build
COPY src ./src
RUN mvn -B -DskipTests package

# === Stage 2: Run the JAR in a lightweight runtime image ===
FROM eclipse-temurin:17-jre-jammy

# Working directory inside runtime container
WORKDIR /app

# Copy only the JAR from the builder stage (rename to app.jar)
COPY --from=builder /workspace/target/*.jar app.jar

# Expose port (Cloud Run injects $PORT, defaults to 8080)
EXPOSE 8081

# Run the JAR
ENTRYPOINT ["java","-jar","/app/app.jar"]




#=============OLD VERSION FOR DOCKER RUNNING===============#
#FROM eclipse-temurin:17-jdk-alpine
#WORKDIR /app
#
## Copy the application JAR
#COPY target/InsuredPerson-0.0.1-SNAPSHOT.jar app.jar
#
#EXPOSE 8081
#
## Start the Spring Boot application directly
#ENTRYPOINT ["java", "-jar", "app.jar"]
