# Étape 1 : build de l’application
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : exécuter le jar
FROM eclipse-temurin:17
# 🛠️ Installation de unzip
RUN apt-get update && apt-get install -y unzip && apt-get clean
WORKDIR /app
COPY --from=build /app/target/orthoapp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]