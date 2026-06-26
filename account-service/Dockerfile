# ============================================
# DOCKERFILE — account-service
# ============================================
# Un Dockerfile se lit de haut en bas.
# Chaque instruction crée une "couche" dans l'image Docker.

# ---- ÉTAPE 1 : BUILD ----
# On utilise une image Maven+Java pour compiler le projet
# "AS builder" = on nomme cette étape "builder" pour y référer plus tard
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# On définit le dossier de travail dans le container
WORKDIR /app

# On copie d'abord le pom.xml SEUL pour profiter du cache Docker
# Si le pom.xml ne change pas, Docker ne retélécharge pas les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Maintenant on copie le code source
COPY src ./src

# On compile le projet et on crée le fichier .jar
# -DskipTests = on ne lance pas les tests pendant le build Docker
RUN mvn clean package -DskipTests

# ---- ÉTAPE 2 : RUN ----
# On repart d'une image légère (juste Java, sans Maven)
# Cela réduit la taille finale de l'image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# On copie UNIQUEMENT le .jar généré depuis l'étape builder
COPY --from=builder /app/target/*.jar app.jar

# On expose le port sur lequel le service écoute
EXPOSE 8082

# Commande exécutée au démarrage du container
ENTRYPOINT ["java", "-jar", "app.jar"]
