# Build stage ensures reproducible Maven build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml pom.xml
COPY src src
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B clean package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
