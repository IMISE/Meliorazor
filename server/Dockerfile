# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
COPY . .
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f ./pom.xml clean package -DskipTests

# Package stage
FROM eclipse-temurin:25-jre-jammy
ARG JAR_FILE=./target/*.jar
COPY --from=build $JAR_FILE ./server/runner.jar
EXPOSE 8081
ENTRYPOINT java -jar ./server/runner.jar

