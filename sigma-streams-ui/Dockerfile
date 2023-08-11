# docker build --platform linux/amd64 --push -t michaelpeacock/confluent-sigma-ui:test .
# docker push michaelpeacock/confluent-sigma-ui:test

FROM openjdk:11
RUN apt-get update && apt-get upgrade -y \
 && useradd --create-home --shell /bin/bash appuser \
 && rm -rf /var/lib/apt/lists/*
COPY ./target/sigma-streams-ui-test.jar /tmp
USER appuser
WORKDIR /tmp
CMD java -jar sigma-streams-ui-test.jar
