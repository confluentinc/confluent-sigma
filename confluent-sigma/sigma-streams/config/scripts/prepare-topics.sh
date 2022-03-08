kafka-topics --bootstrap-server 127.0.0.1:9092 --topic sigma-rules --replication-factor 1 --partitions 1 \
  --config cleanup.policy=compact --config retention.ms=-1 --create