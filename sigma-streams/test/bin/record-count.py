#!python3   
#
# This script is used to count the number of records in a Kafka topic.
#
# USAGE:
#   python3 record-count.py <bootstrap_servers> <sasl_username> <sasl_password> <topic> [options]
#
# PARAMETERS:
#   bootstrap_servers  - Kafka cluster bootstrap servers (e.g., "pkc-abc123.us-east-1.aws.confluent.cloud:9092")
#   sasl_username     - SASL username for authentication
#   sasl_password     - SASL password for authentication  
#   topic             - Name of the Kafka topic to count records in
#
# OPTIONS:
#   -v, --verbose     - Enable verbose output showing detailed partition information
#   -p, --poll_time   - Poll timeout in seconds to fetch the first message (default: 10)
#
# EXAMPLES:
#   python3 record-count.py "pkc-abc123.us-east-1.aws.confluent.cloud:9092" "myuser" "mypass" "my-topic"
#   python3 record-count.py "localhost:9092" "user" "pass" "test-topic" -v -p 5
#
# NOTES:
#   - Designed for Confluent Cloud with SASL_SSL authentication
#   - Provides approximate record count based on watermark offsets
#   - Creates a unique consumer group for each run
#   - Handles SIGINT gracefully for clean exit


import datetime
import argparse
import signal
import sys

from confluent_kafka import Consumer, TopicPartition, KafkaException
from confluent_kafka.admin import AdminClient

TIMEOUT = 10


def signal_handler(sig, frame):
    """Handle SIGINT (Ctrl-C) gracefully"""
    print("\nInterrupted by user. Exiting...")
    # Force exit even if cleanup doesn't work
    import os
    os._exit(130)


def count_records(bootstrap_servers, sasl_username, sasl_password, topic):
    consumer = None

    try:
        # Validate bootstrap servers
        if not bootstrap_servers or bootstrap_servers.strip() == '':
            raise ValueError("Bootstrap servers cannot be empty")
            
        # Create an AdminClient to fetch partition information
        admin_client = AdminClient({
            'bootstrap.servers': bootstrap_servers,
            'security.protocol': 'SASL_SSL',
            'sasl.mechanism': 'PLAIN',
            'sasl.username': sasl_username,
            'sasl.password': sasl_password
        })

        # Fetch partition information for the topic with timeout
        topic_metadata = admin_client.list_topics(topic, timeout=10)
        if args.verbose:
            print("Retrieved metadata for topic " + str(topic_metadata))

        current_datetime = datetime.datetime.now()
        milliseconds_after_epoch = current_datetime.timestamp() * 1000
        consumer_group_id = "record-count-" + str(int(milliseconds_after_epoch))

        if args.verbose:
            print("Consumer group = " + consumer_group_id)

        # Create a Kafka consumer
        consumer = Consumer({
            'bootstrap.servers': bootstrap_servers,
            'security.protocol': 'SASL_SSL',
            'sasl.mechanism': 'PLAIN',
            'sasl.username': sasl_username,
            'sasl.password': sasl_password,
            'group.id': consumer_group_id,
            'auto.offset.reset': 'earliest',
            'enable.auto.commit': 'false'
        })

        # Read one message from each partition
        messages = {}
        topic_partitions = []

        for partition in topic_metadata.topics[topic].partitions.keys():
            tp = TopicPartition(topic, partition)
            topic_partitions.append(tp)
            consumer.assign([tp])

            msg = consumer.poll(args.poll_time)
            if msg is None:
                print("No message polled for partition " + str(tp.partition) + " in " + str(args.poll_time) +
                      " seconds. Without a committed offset the total is likely to be inaccurate unless no data " +
                      "has been flushed due to retention policy")
            else:
                if args.verbose:
                    print("Polled message from partition " + str(tp.partition))
                messages[partition] = msg.value().decode('utf-8')
                consumer.commit(message=msg, asynchronous=False)

        committed_offsets = consumer.committed(topic_partitions)
        if args.verbose:
            print("Retrieved committed offsets message for " + str(len(committed_offsets)) + " partitions")

        # Calculate consumer lag for each partition
        consumer_lag = {}
        total_lag = 0
        for p in committed_offsets:

            if args.verbose:
                print("For parition " + str(p.partition) + " committed offset is " + str(p.offset))

            watermark_offsets = consumer.get_watermark_offsets(p)
            high_watermark = watermark_offsets[1]

            if args.verbose:
                print("For parition " + str(p.partition) + " watermark offsets are " +
                      str(watermark_offsets))

            if high_watermark > 0:
                if p.offset < 0:
                    lag = high_watermark
                else:
                    # if there is a valid committed offset then its one more then the single message we polled
                    lag = high_watermark - p.offset + 1

                consumer_lag[p.partition] = lag
                total_lag = total_lag + lag
            else:
                if args.verbose:
                    print("Skipping calculation for partition " + str(p.partition) +
                          " since there is weirdness in committed offset " + str(p.offset) +
                          " and hig watermark " + str(high_watermark) +
                          ". Probably this means there are no messages in the partition")

        consumer.close()
        return total_lag
    finally:
        if consumer is not None:
            consumer.close()


if __name__ == "__main__":
    # Register signal handler for graceful interruption
    signal.signal(signal.SIGINT, signal_handler)
    
    parser = argparse.ArgumentParser(description="Provide an estimated record count for messages in a topic based " +
                                     "on minimum offset and water marks.  Currently built to work on Confluent Cloud " +
                                     "based on SASL_SSL authentication but could easily be adapted for other auths")

    # Required parameters
    parser.add_argument("bootstrap_servers", help="boostrap server for kafka cluster")
    parser.add_argument("sasl_username", help="sasl username")
    parser.add_argument("sasl_password", help="sasl password")
    parser.add_argument("topic", help="topic you want a record count for")

    # Optional flag argument
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable verbose mode")
    parser.add_argument("-p", "--poll_time", type=int, help="specify the poll timeout to fetch the first message",
                        default=TIMEOUT)

    args = parser.parse_args()

    # bootstrap_servers = sys.argv[1]
    # sasl_username = sys.argv[2]
    # sasl_password = sys.argv[3]
    # topic_name = sys.argv[4]

    # Count records
    record_count = count_records(args.bootstrap_servers, args.sasl_username, args.sasl_password, args.topic)
    formatted_count = format(record_count, ",")
    if args.verbose:
        print("")

    print(f"Approximate total records in topic '{args.topic}': " + formatted_count)
