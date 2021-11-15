package io.confluent.sigmarules.streams;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StreamManager {
    public Properties properties = new Properties();

    public StreamManager(Properties properties) {
        this.properties.putAll(properties);
        this.properties.put(StreamsConfig.APPLICATION_ID_CONFIG, properties.getProperty("application.id"));
        this.properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.server"));
        this.properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        this.properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    }

    public Properties getStreamProperties() {
        return this.properties;
    }

    public void createTopic(String topicName) {
        createTopic(topicName, 1, 1);
    }

    public void createTopic(String topicName, int numPartitions, int replicationFactor) {
        AdminClient client = AdminClient.create(getStreamProperties());
        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(topicName, numPartitions, (short)replicationFactor));
        client.createTopics(topics);
        client.close();
    }


}
