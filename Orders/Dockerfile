# Этап сборки (используем Maven + JDK)
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Собираем JAR, пропуская тесты
RUN mvn clean package -DskipTests

# Этап запуска (только JRE)
FROM eclipse-temurin:17-jre
WORKDIR /app
# Копируем собранный JAR из первого этапа
COPY --from=builder /app/target/Orders-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]