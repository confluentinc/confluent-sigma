FROM openjdk:11
RUN apt-get update && apt-get upgrade -y \
  && useradd --no-log-init --create-home --shell /bin/bash appuser \
  && rm -rf /var/lib/apt/lists/

COPY ./target/sigma-streams-1.3-fat.jar /tmp
USER appuser
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "sigma-streams-1.3.0-fat.jar"]
CMD java -jar sigma-streams-1.3.0-fat.jar
