FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# === Runtime Stage ===
FROM python:3.10-slim as runtime

# Встановлюємо Java
RUN apt-get update && apt-get install -y openjdk-17-jre curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Встановлюємо залежності PaddleOCR
RUN pip install flask paddleocr opencv-python

# Копіюємо jar-файл зі стадії build
COPY --from=build /target/highlight-server-spring-0.0.1-SNAPSHOT.jar /app/highlightserverspring.jar

# Копіюємо paddle_server.py
COPY paddle_server.py /app/paddle_server.py

WORKDIR /app

# === Запускаємо Python + Java одночасно через bash ===
EXPOSE 8080 5050

CMD bash -c "python3 paddle_server.py & java -jar highlightserverspring.jar"
