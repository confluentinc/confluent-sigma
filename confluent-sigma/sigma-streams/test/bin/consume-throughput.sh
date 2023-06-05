#!/bin/bash

# These scripts are intended to be run from the test directory
#
# This script takes two arguments
# Argument 1 is the topic to read from
# Argument 2 is how many simultaneous kafka-consumer-perf-tests should I run.


EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]; then
    echo "Usage: $(basename "$0") <topic> <concurrent-consumers>"
    exit -1
fi

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
bootstrap_value=$(grep "^$bootstrap_key=" "$SIGMA_PROPS" | cut -d'=' -f2-)

# Export the property value
export "$bootstrap_key=$bootstrap_value"

CGROUP="CONSUMER-PERF-$1-$RANDOM"

for i in `seq $2`
do	
  docker run -v ${SIGMA_PROPS_DIR}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
       kafka-consumer-perf-test --bootstrap-server ${bootstrap_value} \
       --messages 20000000 --consumer.config /mnt/config/sigma.properties --topic $1 --print-metrics --group $CGROUP > /tmp/res$i.txt &
done
