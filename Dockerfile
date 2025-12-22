FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src

ARG RUN_TESTS=false

RUN if [ "$RUN_TESTS" = "true" ]; then \
      ./mvnw -q clean verify; \
    else \
      ./mvnw -q -DskipTests clean package; \
    fi

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ARG SERVER_PORT=8080
ENV SERVER_PORT=${SERVER_PORT}

EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java","-jar","/app/app.jar"]