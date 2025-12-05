FROM eclipse-temurin:21-jdk

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

WORKDIR /app

COPY . .

RUN chmod +x gradlew scripts/*.sh
RUN chown -R appuser:appgroup /app

RUN chmod +x gradlew && \
    mkdir -p /home/gradle/.gradle && \
    chmod -R 777 /home/gradle/.gradle

USER root

EXPOSE 8080

# RUN ./gradlew build -x test

CMD ["./scripts/wait-for-db.sh", "postgres", "5432", "java", "-jar", "build/smart-parking.jar"]