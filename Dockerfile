FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

COPY target/salary-management-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
