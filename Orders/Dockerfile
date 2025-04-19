# Этап сборки слоев приложения
FROM bellsoft/liberica-openjre-debian:24.0.1 AS layers
WORKDIR /application
COPY target/Orders-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Финальный образ
FROM bellsoft/liberica-openjre-debian:24.0.1

# Настройка окружения
VOLUME /tmp
RUN useradd -ms /bin/bash spring && \
    mkdir -p /app && \
    chown spring:spring /app
WORKDIR /app
USER spring

# Копирование слоев приложения
COPY --from=layers --chown=spring:spring /application/dependencies/ ./
COPY --from=layers --chown=spring:spring /application/spring-boot-loader/ ./
COPY --from=layers --chown=spring:spring /application/snapshot-dependencies/ ./
COPY --from=layers --chown=spring:spring /application/application/ ./

# Healthcheck и запуск
HEALTHCHECK --interval=30s --timeout=5s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]