# ---- Etape de construction ----
FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

# Node.js est requis par la compilation des assets Tailwind (asset-pipeline)
RUN apt-get update \
    && apt-get install -y --no-install-recommends nodejs npm \
    && rm -rf /var/lib/apt/lists/*

# Dependances npm (tailwindcss) avant la copie du code source
COPY package.json package-lock.json ./
RUN npm install

COPY . .

# Construit un WAR executables (Spring Boot launcher inclus).
# Le nettoyage evite de laisser des artefacts de developpement.
ARG APP_VERSION=2.0.0
RUN ./gradlew clean war --no-daemon

# ---- Etape d'execution ----
FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx768m -XX:MaxMetaspaceSize=512m -Duser.timezone=UTC" \
    EQUIPMENTS_DB_URL="jdbc:postgresql://db:5432/equipments" \
    EQUIPMENTS_DB_USER="equipments" \
    EQUIPMENTS_DB_PASSWORD=""

ARG APP_VERSION=2.0.0
COPY --from=build /workspace/build/libs/Proj-Equipment-${APP_VERSION}.war app.war

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://127.0.0.1:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.war"]