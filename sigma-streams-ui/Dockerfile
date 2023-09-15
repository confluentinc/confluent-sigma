FROM openjdk:11
RUN apt-get update && apt-get upgrade -y \
 && useradd --create-home --shell /bin/bash appuser \
 && rm -rf /var/lib/apt/lists/*
COPY ./target/sigma-streams-ui-1.3.jar /tmp
USER appuser
WORKDIR /tmp
CMD java -jar sigma-streams-ui-1.3.jar
