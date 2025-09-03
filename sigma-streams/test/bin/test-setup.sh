#!/bin/bash

# These scripts are intended to be run from the test directory
#
# Assumes data already loaded
#
# This script takes two arguments
# Argument 1 is the test case
# Argument 2 is the number of threads to use for the benchmark

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]; then
    echo "Usage: $(basename "$0") <test_case>"
    exit -1
else
  TOPIC=$1
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

kafka-topics --bootstrap-server $BOOTSTRAP_SERVERS --command-config $SIGMA_PROPS --delete --topic sigma-rules
"$SIGMA_STREAMS_BIN"/sigma-loader.sh -d "$SCRIPT_DIR"/../benchmarks/rules/$1 -c $SIGMA_PROPS

cp "$SCRIPT_DIR"/../benchmarks/configs/$1/benchmark.properties "$SIGMA_PROPS_DIR"
echo "bootstrap.servers=$BOOTSTRAP_SERVERS" >> "$SIGMA_PROPS_DIR"/benchmark.properties
grep "^sasl.jaas.config" "$SIGMA_PROPS" >> "$SIGMA_PROPS_DIR"/benchmark.properties
datetime=$(date +"%Y%m%d%H%M%S")
echo "application.id=confluent-streams-benchmark-$1-$datetime" >> "$SIGMA_PROPS_DIR"/benchmark.properties


# Setup sigma-cc-admin.properties alongside benchmark.properties
SCHEMA_REGISTRY=$(grep '^schema.registry=' "$SIGMA_PROPS" | cut -d'=' -f2-)
TARGET_CC_ADMIN="$SIGMA_PROPS_DIR"sigma-cc-admin.properties

# Copy template and populate values
cp -f "$SCRIPT_DIR"/../config/sigma-cc-admin.properties "$TARGET_CC_ADMIN"

# Update bootstrap.servers and schema.registry in-place (macOS-compatible sed)
if [ -n "$BOOTSTRAP_SERVERS" ]; then
  sed -i'' "s|^bootstrap.servers=.*|bootstrap.servers=$BOOTSTRAP_SERVERS|" "$TARGET_CC_ADMIN"
fi
if [ -n "$SCHEMA_REGISTRY" ]; then
  sed -i'' "s|^schema.registry=.*|schema.registry=$SCHEMA_REGISTRY|" "$TARGET_CC_ADMIN"
fi

# Generate rest.auth.token from KAFKA_SASL credentials
if [ -n "$KAFKA_SASL_USERNAME" ] && [ -n "$KAFKA_SASL_PASSWORD" ]; then
  REST_AUTH_TOKEN=$(echo -n "$KAFKA_SASL_USERNAME:$KAFKA_SASL_PASSWORD" | base64 | tr -d '\n')
  if grep -q '^rest.auth.token=' "$TARGET_CC_ADMIN"; then
    sed -i'' "s|^rest.auth.token=.*|rest.auth.token=$REST_AUTH_TOKEN|" "$TARGET_CC_ADMIN"
  else
    echo "rest.auth.token=$REST_AUTH_TOKEN" >> "$TARGET_CC_ADMIN"
  fi
else
  if ! grep -q '^rest.auth.token=' "$TARGET_CC_ADMIN"; then
    echo "# Add your Base64 encoded key:secret below" >> "$TARGET_CC_ADMIN"
    echo "rest.auth.token=" >> "$TARGET_CC_ADMIN"
  fi
fi
