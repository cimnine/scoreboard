FROM eclipse-temurin:17-jdk-jammy AS unpack
WORKDIR /tmp/distribution
COPY assembly/{{distributionArtifactFile}} distribution.zip
RUN jar xf distribution.zip && mv scoreboard /opt/scoreboard

FROM {{dockerBaseImage}}
COPY --from=unpack /opt/scoreboard /opt/scoreboard
RUN mkdir -p /opt/scoreboard/config/autosave /opt/scoreboard/html/game-data && \
    chmod +x "/opt/scoreboard/bin/CRG Scoreboard" && \
    chown -R 10001:10001 /opt/scoreboard
WORKDIR /opt/scoreboard
USER 10001:10001
EXPOSE 8000
VOLUME ["/opt/scoreboard/config/autosave", "/opt/scoreboard/html/game-data"]
ENTRYPOINT ["/opt/scoreboard/bin/CRG Scoreboard"]
CMD ["--nogui", "--nodiscovery", "--import="]
