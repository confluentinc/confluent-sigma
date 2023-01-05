# docker build -t michaelpeacock/confluent-sigma:v1 .
# docker push michaelpeacock/confluent-sigma:v1

FROM openjdk:11
COPY ./target/sigma-streams-1.2.1-fat.jar /tmp
WORKDIR /tmp
CMD java -cp sigma-streams-1.2.1-fat.jar io.confluent.sigmarules.Application -c /tmp/config/sigma.properties
