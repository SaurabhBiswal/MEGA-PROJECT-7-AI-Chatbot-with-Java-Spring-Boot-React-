# Build stage
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY backend/src ./backend/src
# Limit Maven memory usage
ENV MAVEN_OPTS="-Xmx512m -Xms256m"
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar (handle potential submodules if needed, but assuming root pom builds the app)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Use Container Support flags to respect memory limits
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-jar", "app.jar"]
