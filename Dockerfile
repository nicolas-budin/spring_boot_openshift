# --- Etape de build ---
FROM registry.access.redhat.com/ubi9/openjdk-17:latest AS build
USER root
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Build avec le wrapper Maven fourni par l'image UBI (mvn est deja present)
RUN mvn -q -B -DskipTests package

# --- Etape finale (image d'execution minimale) ---
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:latest

WORKDIR /deployments
COPY --from=build /build/target/hello-openshift.jar /deployments/app.jar

# OpenShift execute les conteneurs avec un UID arbitraire et le groupe root (0)
# -> il faut que les fichiers soient lisibles par le groupe root
USER 185

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]
