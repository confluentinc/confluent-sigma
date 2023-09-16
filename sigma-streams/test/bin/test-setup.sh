#!/bin/bash

# These scripts are intended to be run from the test directory
#
# Assumes data already loaded
#
# This script takes one arguments
# Argument 1 is the test case

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]; then
    echo "Usage: $(basename "$0") <test_case>"
    exit -1
else
  TOPIC=$1
  NUM_THREADS=$2
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  SIGMA_STREAMS_BIN="$SCRIPT_DIR/../../bin"
  source $SIGMA_STREAMS_BIN/auto-configure.sh
fi

if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

bootstrap_key="bootstrap.servers"
BOOTSTRAP_SERVER=$(grep "^$bootstrap_key=" "$SIGMA_PROPS" | cut -d'=' -f2-)

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --command-config $SIGMA_PROPS --delete --topic sigma-rules
"$SIGMA_STREAMS_BIN"/sigma-loader.sh -d "$SCRIPT_DIR"/../benchmarks/rules/$1 -c $SIGMA_PROPS

cp "$SCRIPT_DIR"/../benchmarks/configs/$1/benchmark.properties "$SIGMA_PROPS_DIR"
echo "$bootstrap_key=$BOOTSTRAP_SERVER" >> "$SIGMA_PROPS_DIR"/benchmark.properties
grep "^sasl.jaas.config" "$SIGMA_PROPS" >> "$SIGMA_PROPS_DIR"/benchmark.properties
datetime=$(date +"%Y%m%d%H%M%S")
echo "application.id=confluent-streams-benchmark-$1-$datetime" >> "$SIGMA_PROPS_DIR"/benchmark.properties