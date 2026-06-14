# ---- Build stage ----
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# Copy wrapper + pom first so dependency layer is cached
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Then source — only this layer rebuilds on code changes
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:23-jre AS runtime
WORKDIR /app

# Run as non-root
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]