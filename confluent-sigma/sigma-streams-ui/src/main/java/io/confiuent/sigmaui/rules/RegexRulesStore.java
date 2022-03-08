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

package io.confiuent.sigmaui.rules;

import io.confiuent.sigmaui.models.RegexRule;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.kcache.Cache;
import io.kcache.CacheUpdateHandler;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component
public class RegexRulesStore implements CacheUpdateHandler<String, RegexRule> {
    static final Logger logger = LogManager.getLogger(RegexRulesStore.class);

    private Cache<String, RegexRule> regexRulesCache;

    private RegexRuleObserver observer;

    @Value("${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value("${kafka.schemaRegistry}")
    private String schemaRegistry;

    @Value("${confluent.regex.ruleTopic}")
    private String ruleTopic;

    @PostConstruct
    private void initialize() {
        Properties props = new Properties();
        props.setProperty("kafkacache.bootstrap.servers", this.bootstrapAddress);
        props.setProperty("kafkacache.topic", this.ruleTopic);
        props.setProperty("key.converter.schema.registry.url", this.schemaRegistry);
        props.setProperty("value.converter.schema.registry.url", this.schemaRegistry);
        this.regexRulesCache = (Cache<String, RegexRule>)new KafkaCache(new KafkaCacheConfig(props), Serdes.String(), getJsonSerde(), this, null);
        this.regexRulesCache.init();
    }

    public void addObserver(RegexRuleObserver observer) {
        this.observer = observer;
    }

    public static Serde<RegexRule> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", RegexRule.class);
        KafkaJsonSerializer kafkaJsonSerializer = new KafkaJsonSerializer();
        kafkaJsonSerializer.configure(serdeProps, false);
        KafkaJsonDeserializer kafkaJsonDeserializer = new KafkaJsonDeserializer();
        kafkaJsonDeserializer.configure(serdeProps, false);
        return Serdes.serdeFrom((Serializer)kafkaJsonSerializer, (Deserializer)kafkaJsonDeserializer);
    }

    public void addRule(String ruleName, RegexRule rule) {
        this.regexRulesCache.put(ruleName, rule);
    }

    public void removeRule(String ruleName) {
        this.regexRulesCache.remove(ruleName);
    }

    public Set<String> getRuleNames() {
        return this.regexRulesCache.keySet();
    }

    public Cache<String, RegexRule> getRules() {
        return this.regexRulesCache;
    }

    public RegexRule getRule(String ruleName) {
        return this.regexRulesCache.get(ruleName);
    }

    public void handleUpdate(String key, RegexRule value, RegexRule oldValue, TopicPartition tp, long offset, long timestamp) {
        if (this.observer != null) {
            this.observer.handleRuleUpdate(key, value);
        }
    }

    public static void main(String[] args) {}
}
