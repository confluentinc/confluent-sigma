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

package io.confluent.sigmarules.rules;

import io.confluent.sigmarules.SigmaPropertyEnum;
import io.confluent.sigmarules.models.SigmaRule;
import io.kcache.Cache;
import io.kcache.CacheUpdateHandler;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaRulesStore implements CacheUpdateHandler<String, String> {

    final static Logger logger = LogManager.getLogger(SigmaRulesStore.class);

    public static final String KEY_CONVERTER_SCHEMA_REGISTRY_URL = "key.converter.schema.registry.url";
    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";

    private Cache<String, String> sigmaRulesCache;
    private SigmaRuleObserver observer = null;

    public SigmaRulesStore(Properties properties) {
        initialize(properties);
    }

    public void initialize(Properties properties) {
        Properties kcacheProps = new Properties(properties);
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG,
                properties.getProperty(SigmaPropertyEnum.BOOTSTRAP_SERVER.toString()));
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG,
                properties.getProperty(SigmaPropertyEnum.SIGMA_RULES_TOPIC.toString()));

        // optional config parameters
        if (properties.containsKey(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SECURITY_PROTOCOL_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()));

        if (properties.containsKey(SigmaPropertyEnum.SASL_MECHANISM.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SASL_MECHANISM_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SASL_MECHANISM.toString()));

        if (properties.containsKey("sasl.jaas.config"))
            kcacheProps.setProperty("kafkacache.sasl.jaas.config",
                properties.getProperty("sasl.jaas.config"));

        if (properties.containsKey("sasl.client.callback.handler.class"))
            kcacheProps.setProperty("kafkacache.sasl.client.callback.handler.class",
                    properties.getProperty("sasl.client.callback.handler.class"));

        if (properties.containsKey(SigmaPropertyEnum.SCHEMA_REGISTRY.toString())) {
            kcacheProps.setProperty(KEY_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
            kcacheProps.setProperty(VALUE_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
        }

        sigmaRulesCache = new KafkaCache<>(
            new KafkaCacheConfig(kcacheProps),
            Serdes.String(),
            Serdes.String(),
            this,
            null);

        sigmaRulesCache.init();
    }

    public void addObserver(SigmaRuleObserver observer) {
        this.observer = observer;
    }

    public void addRule(String ruleName, String rule) {
        SigmaRule sigmaRule = null;
        if (rule != null) {
            System.out.println("adding rule to cache");
            sigmaRulesCache.put(ruleName, rule);
        }
    }

    public void removeRule(String ruleName) {
        sigmaRulesCache.remove(ruleName);
    }

    public Set<String> getRuleNames() {
        return sigmaRulesCache.keySet();
    }

    public String getRuleAsYaml(String ruleName) {
        if (sigmaRulesCache.containsKey(ruleName)) {
            return sigmaRulesCache.get(ruleName);
        }

        return null;
    }

    Cache<String, String> getRules() {
        return this.sigmaRulesCache;
    }

    @Override
    public void cacheInitialized() {
        CacheUpdateHandler.super.cacheInitialized();
    }

    @Override
    public void handleUpdate(String key, String value, String oldValue, TopicPartition tp, long offset, long timestamp) {

        if (!value.equals(oldValue)) {
            if (observer != null) {
                observer.handleRuleUpdate(key, getRuleAsYaml(key));
            }
        }

    }
}
