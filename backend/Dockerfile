FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=builder --chown=spring:spring /app/build/libs/app.jar app.jar

USER spring
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
CMD ["java", "-jar", "app.jar"]
