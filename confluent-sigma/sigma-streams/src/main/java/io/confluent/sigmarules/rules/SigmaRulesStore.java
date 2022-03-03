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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.utilities.YamlUtils;
import io.kcache.Cache;
import io.kcache.CacheUpdateHandler;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.Set;

public class SigmaRulesStore implements CacheUpdateHandler<String, SigmaRule> {
    final static Logger logger = LogManager.getLogger(SigmaRulesStore.class);
    private Cache<String, SigmaRule> sigmaRulesCache;
    private SigmaRuleObserver observer = null;

    public SigmaRulesStore(Properties properties) {
        initialize(properties);
    }

    public void initialize(Properties properties) {
        Properties props = new Properties();
        props.setProperty("kafkacache.bootstrap.servers", properties.getProperty("bootstrap.server"));
        props.setProperty("kafkacache.topic", properties.getProperty("sigma.rules.topic"));
        props.setProperty("key.converter.schema.registry.url", properties.getProperty("schema.registry"));
        props.setProperty("value.converter.schema.registry.url", properties.getProperty("schema.registry"));
        sigmaRulesCache = new KafkaCache<>(new KafkaCacheConfig(props), Serdes.String(), SigmaRule.getJsonSerde(),
                this, null);
        sigmaRulesCache.init();
    }

    public void addObserver(SigmaRuleObserver observer) {
        this.observer = observer;
    }

    public void addRule(String ruleName, String rule) {
        SigmaRule sigmaRule = null;
        try {
            if (rule != null) {
                sigmaRule = YamlUtils.getYAMLMapper().readValue(rule, SigmaRule.class);
                System.out.println("adding rule to cache");
                sigmaRulesCache.put(ruleName, sigmaRule);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        };
    }

    public void removeRule(String ruleName) {
        sigmaRulesCache.remove(ruleName);
    }

    public Set<String> getRuleNames() {
        return sigmaRulesCache.keySet();
    }

    public String getRuleAsYaml(String ruleName) {
        if (sigmaRulesCache.containsKey(ruleName)) {
            return sigmaRulesCache.get(ruleName).toString();
        }

        return null;
    }

    Cache<String, SigmaRule> getRules() {
        return this.sigmaRulesCache;
    }

    public SigmaRule getRule(String ruleName) {
        SigmaRule sigmaRule = null;

        try {
            String rule = getRuleAsYaml(ruleName);
            if (rule != null) {
                sigmaRule = YamlUtils.getYAMLMapper().readValue(rule, SigmaRule.class);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        };

        return sigmaRule;
    }

    @Override
    public void handleUpdate(String key, SigmaRule value, SigmaRule oldValue, TopicPartition tp, long offset, long timestamp) {

        if (oldValue == null || (oldValue != null && !value.equals(oldValue))) {
            if (observer != null) {
                observer.handleRuleUpdate(key, getRuleAsYaml(key));
            }
        }

    }
}
