FROM --platform=$BUILDPLATFORM openjdk:11
COPY ./target/sigma-streams-1.3.0-fat.jar /tmp
WORKDIR /tmp
ENTRYPOINT java -cp sigma-streams-1.3.0-fat.jar io.confluent.sigmarules.SigmaStreamsApp