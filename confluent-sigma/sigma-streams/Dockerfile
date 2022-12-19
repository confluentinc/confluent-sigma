FROM openjdk:11
COPY ./target/sigma-streams-1.3.0-fat.jar /tmp
WORKDIR /tmp
CMD java -cp sigma-streams-1.3.0-fat.jar io.confluent.sigmarules.SigmaStreamsApp -c /tmp/config/sigma.properties
