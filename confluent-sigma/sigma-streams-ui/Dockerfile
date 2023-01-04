# docker build -t michaelpeacock/confluent-sigma-regex-ui:latest .
# docker push michaelpeacock/confluent-sigma-regex-ui:latest

FROM openjdk:11
RUN apt-get update && apt-get upgrade -y \
 && rm -rf /var/lib/apt/lists/*
COPY ./target/sigma-streams-ui-1.2.1.jar /tmp
WORKDIR /tmp
CMD java -jar sigma-streams-ui-1.2.1.jar
