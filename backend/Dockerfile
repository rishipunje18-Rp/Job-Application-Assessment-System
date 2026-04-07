# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/job-portal-1.0.0.jar app.jar

EXPOSE 8081
ENV SERVER_PORT=8081

ENTRYPOINT ["java", "-jar", "app.jar"]
