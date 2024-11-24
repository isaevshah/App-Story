# Этап сборки
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . /app/
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Этап выполнения
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
