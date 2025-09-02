#!/bin/bash

# These scripts are intended to be run from the test directory
#
# This script takes one argument.  The topic to get a record count from
#

# Handle Ctrl-C (SIGINT) and SIGTERM properly
trap 'echo "\nInterrupted. Terminating..."; exit 130' SIGINT SIGTERM

# Argument 1 is the topic to read from (required)
# Argument 2 true or false as to whether to be verbose (optional)

# Usage message
usage() {
  echo "Usage: $0 <topic> [true|false]"
  echo "  <topic>      Kafka topic to get record count from (required)"
  echo "  [true|false] Verbose output (optional, default: false)"
}

# Check for required topic argument
if [ -z "$1" ]; then
  usage
  exit 1
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  SIGMA_STREAMS_BIN="$SCRIPT_DIR/../../bin"
  source $SIGMA_STREAMS_BIN/auto-configure.sh
fi

if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

# Check if the second argument is true
if [[ "$2" = true ]]; then
  exec python3 "$SCRIPT_DIR"/record-count.py -v "$BOOTSTRAP_SERVERS" "$KAFKA_SASL_USERNAME" "$KAFKA_SASL_PASSWORD" $1
else
  exec python3 "$SCRIPT_DIR"/record-count.py "$BOOTSTRAP_SERVERS" "$KAFKA_SASL_USERNAME" "$KAFKA_SASL_PASSWORD" $1
fi

