FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY src ./src
COPY web ./web
RUN javac src/*.java
EXPOSE 8080
CMD ["java", "-cp", "src", "WebATMServer"]