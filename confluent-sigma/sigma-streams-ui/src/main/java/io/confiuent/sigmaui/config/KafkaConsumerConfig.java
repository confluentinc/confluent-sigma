/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package io.confiuent.sigmaui.config;

import io.confiuent.sigmaui.models.DNSStreamData;
import io.confluent.sigmarules.models.DetectionResults;
import java.util.HashMap;
import java.util.Map;

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
    @Value("${bootstrap.server}")
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
