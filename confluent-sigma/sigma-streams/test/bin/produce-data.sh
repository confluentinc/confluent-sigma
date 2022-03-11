#!/bin/bash

kafka-topics \
   --bootstrap-server `grep "^\s*bootstrap.server" $HOME/.confluent/java.config | tail -1` \
   --command-config $HOME/.confluent/java.config \
   --topic test \
   --create \
   --replication-factor 3 \
   --partitions $2

while read p; do
  echo "$p" | kafka-console-producer \
                 --topic test \
                 --broker-list `grep "^\s*bootstrap.server" $HOME/.confluent/java.config | tail -1` \
                 --producer.config $HOME/.confluent/java.config
done < $1



