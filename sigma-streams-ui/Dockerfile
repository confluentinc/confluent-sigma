# docker build -t michaelpeacock/confluent-sigma-regex-ui:latest .
# docker push michaelpeacock/confluent-sigma-regex-ui:latest

FROM openjdk:11
COPY ./target/sigma-streams-ui-1.0.jar /tmp
WORKDIR /tmp
CMD java -jar sigma-streams-ui-1.0.jar