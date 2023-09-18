#!/bin/bash

# These scripts are intended to be run from the test directory
#
# This script takes one argument.  The topic to get a record count from
#
# Argument 1 is the topic to read from
# Argument 2 true or false as to whether to be verbose

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  SIGMA_STREAMS_BIN="$SCRIPT_DIR/../../bin"
  source $SIGMA_STREAMS_BIN/auto-configure.sh
fi

if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

BOOTSTRAP_KEY="bootstrap.servers"
BOOTSTRAP=$(grep "^$BOOTSTRAP_KEY=" "$SIGMA_PROPS" | cut -d'=' -f2-)

KAFKA_SASL_USERNAME=$(grep -Eo "username='(.*?)'" "$SIGMA_PROPS"  | sed -E "s/username='//g")
KAFKA_SASL_USERNAME=$(echo $KAFKA_SASL_USERNAME | tr -d "'")

KAFKA_SASL_PASSWORD=$(grep -Eo "password='(.*?)'" "$SIGMA_PROPS" | sed -E "s/password='//g")
KAFKA_SASL_PASSWORD=$(echo $KAFKA_SASL_PASSWORD | tr -d "'")

# Check if the second argument is true
if [[ "$2" = true ]]; then
  python3 "$SCRIPT_DIR"/record-count.py -v "$BOOTSTRAP" "$KAFKA_SASL_USERNAME" "$KAFKA_SASL_PASSWORD" $1
else
  python3 "$SCRIPT_DIR"/record-count.py "$BOOTSTRAP" "$KAFKA_SASL_USERNAME" "$KAFKA_SASL_PASSWORD" $1
fi

