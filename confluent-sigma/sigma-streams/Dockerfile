# docker build -t michaelpeacock/confluent-sigma:v1 .
# docker push michaelpeacock/confluent-sigma:v1

FROM openjdk:11
COPY ./target/sigma-streams-1.0-fat.jar /tmp
WORKDIR /tmp
CMD java -cp sigma-streams-1.0-fat.jar io.confluent.sigmarules.SigmaStreamsApp -c /tmp/config/sigma-dns.properties