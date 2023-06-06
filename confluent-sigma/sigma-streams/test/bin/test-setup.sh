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
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi

if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

bootstrap_key="bootstrap.server"
BOOTSTRAP_SERVER=$(grep "^$bootstrap_key=" "$SIGMA_PROPS" | cut -d'=' -f2-)

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --command-config $SIGMA_PROPS --delete --topic sigma-rules
bin/sigma-loader.sh -dir test/rules/$1
