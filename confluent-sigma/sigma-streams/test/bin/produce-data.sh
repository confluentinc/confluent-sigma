#!/bin/bash

source test/bin/ccloud-env.sh

kafka-topics \
   --bootstrap-server `grep "^\s*bootstrap.server" $HOME/.confluent/java.config | tail -1` \
   --command-config $HOME/.confluent/java.config \
   --topic test \
   --create \
   --replication-factor 3 \
   --partitions $2


docker run -v $(pwd)/test/data:/mnt/data --rm --network=host edenhill/kafkacat:1.5.0  \
  kafkacat -b ${CCLOUD_BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
  -X sasl.username=${CCLOUD_API_KEY} -X sasl.password=${CCLOUD_API_SECRET} -t test -l /mnt/data/test-zeek-dns.txt




