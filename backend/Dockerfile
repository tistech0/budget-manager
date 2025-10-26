# Dockerfile pour le backend Quarkus - Multi-stage build
# Stage 1: Build the application
FROM registry.access.redhat.com/ubi9/openjdk-21:1.20 AS builder

# Install Maven
USER root
RUN microdnf install -y maven && microdnf clean all

# Set working directory
WORKDIR /build

# Copy pom.xml and download dependencies (better layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with production profile
# This ensures the app is built with PostgreSQL support
RUN mvn clean package -DskipTests -Dquarkus.profile=prod

# Stage 2: Runtime image
FROM registry.access.redhat.com/ubi9/openjdk-21:1.20

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 --from=builder /build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /build/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"