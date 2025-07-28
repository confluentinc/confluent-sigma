#!/bin/bash

# This script creates a topic called test and then loops infinitely sending data from a specified file
#
# Parameter 1 the data file to send
# Parameter 2 the topic name
# Parameter 3 the number of partitions to for the topic


# These scripts are intended to be run from the test directory

# Check for required parameters
if [ $# -lt 3 ]; then
  echo "Usage: $0 <data-file> <topic-name> <num-partitions>"
  echo "  <data-file>      The data file to send"
  echo "  <topic-name>     The topic name"
  echo "  <num-partitions> The number of partitions for the topic"
  exit 1
fi

DATA_FILE_PATH="$1"
if [ ! -f "$DATA_FILE_PATH" ]; then
  echo "Error: Data file '$DATA_FILE_PATH' does not exist."
  exit 1
fi

DATA_FILE_DIR=$(dirname "$DATA_FILE_PATH")
DATA_FILE_BASE=$(basename "$DATA_FILE_PATH")
# Convert relative path to absolute path for Docker volume mounting
DATA_FILE_DIR=$(cd "$DATA_FILE_DIR" && pwd)

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi

if [ ! -f "$SIGMA_PROPS" ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit
fi

docker run --name sigma-create-topic -v ${SIGMA_PROPS_DIR}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
  kafka-topics \
  --bootstrap-server ${BOOTSTRAP_SERVER} \
  --command-config /mnt/config/${SIGMA_PROPS_FILENAME} \
  --topic $2 \
  --create \
  --replication-factor 3 \
  --partitions $3


while true
do
  docker run --name sigma-produce-data -v "$DATA_FILE_DIR":/mnt/data --rm --network=host edenhill/kafkacat:1.5.0 \
    kafkacat -b ${BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username=${KAFKA_SASL_USERNAME} -X sasl.password=${KAFKA_SASL_PASSWORD} -z snappy -t $2 -l /mnt/data/"$DATA_FILE_BASE" || break
done




