#!/bin/bash

# These scripts are intended to be run from the test directory
#
# This script takes two arguments
# Argument 1 is the topic to read from
# Argument 2 is the number of threads.


EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]; then
    echo "Usage: $(basename "$0") <topic> <number-threads>"
    exit -1
else
  TOPIC=$1
  NUM_THREADS=$2
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi


if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi


bootstraps_key="bootstrap.servers"
bootstraps_value=$(grep "^$bootstrap_keys=" "$SIGMA_PROPS" | cut -d'=' -f2-)
CGROUP="CONSUMER-PERF-$TOPIC-$RANDOM"

# Consume 4 million records
docker run -v ${SIGMA_PROPS_DIR}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
     kafka-consumer-perf-test --bootstrap-server ${bootstraps_value} --threads $NUM_THREADS \
     --messages 40000000 --consumer.config /mnt/config/sigma.properties --topic $TOPIC --print-metrics --group $CGROUP










