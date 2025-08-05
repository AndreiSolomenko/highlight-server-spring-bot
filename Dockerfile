FROM maven:3.8.3-openjdk-17 AS build

COPY . .
RUN mvn clean package -DskipTests

# === Runtime Stage ===
FROM python:3.10-slim

# Встановлюємо Java і системні залежності для PaddleOCR
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    curl \
    libglib2.0-0 libsm6 libxrender1 libxext6 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Встановлюємо paddlepaddle (CPU версія), Flask і інші залежності
RUN pip install --no-cache-dir paddlepaddle flask opencv-python

# Потім встановлюємо paddleocr (вона потребує paddlepaddle)
RUN pip install --no-cache-dir paddleocr

# Копіюємо Java jar зі стадії build
COPY --from=build /target/highlight-server-spring-0.0.1-SNAPSHOT.jar /app/highlightserverspring.jar

# Копіюємо Python сервер
COPY paddle_server.py /app/paddle_server.py

WORKDIR /app

EXPOSE 8080 5050

# Запускаємо обидва процеси через tini (щоб коректно обробляти сигнали) у фоновому режимі
RUN apt-get update && apt-get install -y tini && apt-get clean && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["/usr/bin/tini", "--"]

CMD bash -c "python3 paddle_server.py & java -jar highlightserverspring.jar"
