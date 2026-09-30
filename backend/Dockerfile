FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
EXPOSE 8080
ENTRYPOINT ["./gradlew", "bootRun"]
