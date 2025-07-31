FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app
COPY . .
RUN mvn dept44-formatting:apply
ENTRYPOINT ["mvn", "spring-boot:run"]
