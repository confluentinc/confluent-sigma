#!/bin/bash

# This script creates a topic called test and then loops infinitely sending data from a specified file
#
# Parameter 1 the data file to send
# Parameter 2 the topic name
# Parameter 3 the number of partitions to for the topic

source bin/auto-configure.sh

docker run -v $(pwd)/test/config:/mnt/config --rm --network=host confluentinc/cp-server:latest \
  kafka-topics \
  --bootstrap-server ${CCLOUD_BOOTSTRAP_SERVER} \
  --command-config /mnt/config/java.config \
  --topic test \
  --create \
  --replication-factor 3 \
  --partitions $3


while true
do
  docker run -v $(pwd)/test/data:/mnt/data --rm --network=host edenhill/kafkacat:1.5.0  \
    kafkacat -b ${BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username=${KAFKA_SASL_USERNAME} -X sasl.password=${KAFKA_SASL_PASSWORD} -z snappy -t $2 -l /mnt/data/$1
done




