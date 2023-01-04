# docker build -t michaelpeacock/confluent-sigma:v1 .
# docker push michaelpeacock/confluent-sigma:v1

FROM openjdk:11
RUN apt-get update && apt-get upgrade -y \
 && rm -rf /var/lib/apt/lists/*

COPY ./target/sigma-streams-1.2.1-fat.jar /tmp
WORKDIR /tmp
CMD java -cp sigma-streams-1.2.1-fat.jar io.confluent.sigmarules.SigmaStreamsApp -c /tmp/config/sigma.properties
