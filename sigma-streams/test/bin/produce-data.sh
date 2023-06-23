#!/bin/bash

# This script creates a topic called test and then loops infinitely sending data from a specified file
#
# Parameter 1 the data file to send
# Parameter 2 the topic name
# Parameter 3 the number of partitions to for the topic


# These scripts are intended to be run from the test directory

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi


if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit
fi


bootstrap_key="bootstrap.server"
BOOTSTRAP_SERVER=$(grep "^$bootstrap_key=" "$SIGMA_PROPS" | cut -d'=' -f2-)



docker run -v $(pwd)/test/config:/mnt/config --rm --network=host confluentinc/cp-server:latest \
  kafka-topics \
  --bootstrap-server ${BOOTSTRAP_SERVER} \
  --command-config $SIGMA_PROPS \
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




