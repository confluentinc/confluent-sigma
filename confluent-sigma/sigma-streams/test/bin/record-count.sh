#!/bin/bash

# only one parameter which is the topic

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi

if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

bootstrap_key="bootstrap.server"
BOOTSTRAP_SERVER=$(grep "^$bootstrap_key=" "$SIGMA_PROPS" | cut -d'=' -f2-)

CGROUP="COUNT-$1-$RANDOM"

docker run --rm --network=host edenhill/kcat:1.7.1  \
  kafkacat -b ${BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
  -X sasl.username=${KAFKA_SASL_USERNAME} -X sasl.password=${KAFKA_SASL_PASSWORD} -o beginning -G $CGROUP  -c 50000 $1 > /dev/null

docker run -v ${PROPS}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
  kafka-consumer-groups --describe --group $CGROUP --bootstrap-server ${BOOTSTRAP_SERVER} \
  --command-config /mnt/config/sigma.properties



