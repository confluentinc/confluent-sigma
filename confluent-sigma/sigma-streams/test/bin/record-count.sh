#!/bin/bash

source bin/auto-configure.sh

CGROUP="COUNT-$1-$RANDOM"

docker run --rm --network=host edenhill/kcat:1.7.1  \
  kafkacat -b ${BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
  -X sasl.username=${KAFKA_SASL_USERNAME} -X sasl.password=${KAFKA_SASL_PASSWORD} -o beginning -G $CGROUP  -c 50000 $1 > /dev/null

docker run -v ${PROPS}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
  kafka-consumer-groups --describe --group $CGROUP --bootstrap-server ${BOOTSTRAP_SERVER} \
  --command-config /mnt/config/sigma.properties



