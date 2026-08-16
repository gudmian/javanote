FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/
COPY core-api/pom.xml core-api/
COPY push-service/pom.xml push-service/
COPY mvnw .
COPY .mvn .mvn
COPY common/src common/src
COPY core-api/src core-api/src
RUN ./mvnw -pl core-api -am -B package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/core-api/target/core-api-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]