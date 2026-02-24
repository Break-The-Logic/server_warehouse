FROM maven:3.9.12-eclipse-temurin-17 AS deps
WORKDIR /home/nonroot

COPY ./pom.xml .
RUN mvn -B \
        -Dmaven.test.skip=true \
        -ntp dependency:go-offline

FROM deps AS build
WORKDIR /home/nonroot

COPY ./src ./src
RUN mvn -B \
        -Dmaven.test.skip=true package \
        -ntp \
 && java -Djarmode=layertools \
         -jar target/server_warehouse-*.jar extract --destination /home/nonroot/layers

FROM gcr.io/distroless/java17-debian12:nonroot AS runtime
WORKDIR /home/nonroot

ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/urandom -Djava.io.tmpdir=/tmp -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+ExitOnOutOfMemoryError" \
    TZ=UTC

COPY --from=build --chown=nonroot:nonroot /home/nonroot/layers/dependencies/ .
COPY --from=build --chown=nonroot:nonroot /home/nonroot/layers/snapshot-dependencies/ .
COPY --from=build --chown=nonroot:nonroot /home/nonroot/layers/spring-boot-loader/ .
COPY --from=build --chown=nonroot:nonroot /home/nonroot/layers/application/ .

USER nonroot:nonroot

EXPOSE 8080

STOPSIGNAL SIGTERM

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
