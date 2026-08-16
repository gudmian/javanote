FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/
COPY core-api/pom.xml core-api/
COPY push-service/pom.xml push-service/
COPY mvnw .
COPY .mvn .mvn
COPY common/src common/src
COPY push-service/src push-service/src
RUN ./mvnw -pl push-service -am -B package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/push-service/target/push-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]