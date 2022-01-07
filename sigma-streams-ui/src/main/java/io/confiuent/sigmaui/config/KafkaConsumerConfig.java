package io.confiuent.sigmaui.config;

import io.confiuent.sigmaui.models.DNSStreamData;
import java.util.HashMap;
import java.util.Map;

import io.confiuent.sigmaui.models.DetectionResults;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    public ConsumerFactory<String, String> consumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", this.bootstrapAddress);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        return (ConsumerFactory<String, String>)new DefaultKafkaConsumerFactory(props);
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory(groupId));
        return factory;
    }

    public ConsumerFactory<String, DNSStreamData> dnsConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", this.bootstrapAddress);
        props.put("group.id", "regex-consumer");
        return (ConsumerFactory<String, DNSStreamData>)new DefaultKafkaConsumerFactory(props, (Deserializer)new StringDeserializer(), (Deserializer)new JsonDeserializer(DNSStreamData.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DNSStreamData> dnsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DNSStreamData> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(dnsConsumerFactory());
        return factory;
    }

    public ConsumerFactory<String, DetectionResults> dnsDetectionConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", this.bootstrapAddress);
        props.put("group.id", "regex-consumer");
        return (ConsumerFactory<String, DetectionResults>)new DefaultKafkaConsumerFactory(props, (Deserializer)new StringDeserializer(), (Deserializer)new JsonDeserializer(DetectionResults.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DetectionResults> dnsDetectionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DetectionResults> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(dnsDetectionConsumerFactory());
        return factory;
    }
}
