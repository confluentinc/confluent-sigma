#!/bin/bash

# These scripts are intended to be run from the test directory
#

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi


if [ ! -f $SIGMA_PROPS ] ; then
  echo "sigma properties not found.  Suggested path from auto-configure is $SIGMA_PROPS"
  exit -1
fi

saslUsername=$(grep "sasl.jaas.config" $SIGMA_PROPS | sed "s/.*username='\([^']*\)'.*/\1/")
saslPassword=$(grep "sasl.jaas.config" $SIGMA_PROPS | sed "s/.*password='\([^']*\)'.*/\1/")
combined="${saslUsername}:${saslPassword}"
creds=$(echo "$combined" | base64)

curl 'https://api.telemetry.confluent.cloud/v2/metrics/cloud/query' \
-H "Content-Type: application/json" \
-d '{"aggregations":[{"metric":"io.confluent.kafka.server/sent_bytes"}],"filter":{"filters":[{"field":"resource.kafka.id","op":"EQ","value":"lkc-7yyp22"},{"field":"metric.topic","op":"EQ","value":"test2"}],"op":"AND"},"granularity":"PT1M","intervals":["2023-06-07T15:33:00-04:00/2023-06-07T16:33:00-04:00"],"limit":1000}' \
-H "Authorization: Basic $creds"