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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.confluent.sigmarules.config.KcacheConfig;
import io.confluent.sigmarules.config.SigmaPropertyEnum;
import io.confluent.sigmarules.fieldmapping.FieldMapper;
import io.confluent.sigmarules.parsers.ParsedSigmaRule;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import io.kcache.Cache;
import io.kcache.CacheUpdateHandler;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaRulesStore implements CacheUpdateHandler<String, ParsedSigmaRule> {
    final static Logger logger = LogManager.getLogger(SigmaRulesStore.class);

    public static final String KEY_CONVERTER_SCHEMA_REGISTRY_URL = "key.converter.schema.registry.url";
    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";

    private Cache<String, ParsedSigmaRule> sigmaRulesCache;
    private SigmaRuleObserver observer = null;
    
    public SigmaRulesStore(Properties properties) {
        initialize(properties);
    }

    public void initialize(Properties properties) {
        if (properties == null)
            throw new IllegalArgumentException("Cannot initialize SigmaRulesStore with null properties");

        Properties kcacheProps = KcacheConfig.createConfig(properties, SigmaPropertyEnum.SIGMA_RULES_TOPIC);
        System.out.println("kcache properties: " + kcacheProps.toString());

        FieldMapper fieldMapFile = null;
        try {
            if (properties.containsKey("field.mapping.file"))
                fieldMapFile = new FieldMapper(properties.getProperty("field.mapping.file"));
        } catch (IllegalArgumentException | IOException e) {
            logger.info("no field mapping file provided");
        }
        new SigmaRuleParser(fieldMapFile);

        sigmaRulesCache = new KafkaCache<>(
            new KafkaCacheConfig(kcacheProps),
            Serdes.String(),
            ParsedSigmaRule.getJsonSerde(),
            this,
            null);

        sigmaRulesCache.init();
    }

    public void addObserver(SigmaRuleObserver observer) {
        this.observer = observer;
    }

    public void addRule(String rule) {
        ObjectMapper mapper = new ObjectMapper((JsonFactory)new YAMLFactory());
        ParsedSigmaRule sigmaRule = null;
        try {
            if (rule != null) {
                sigmaRule = (ParsedSigmaRule)mapper.readValue(rule, ParsedSigmaRule.class);
                this.sigmaRulesCache.put(sigmaRule.getTitle(), sigmaRule);
            }
        } catch (JsonMappingException e) {
            logger.error("Error loading rule " + rule, e);
        } catch (JsonProcessingException e) {
            logger.error("Error loading rule " + rule, e);
        }
    }

    public Set<String> getRuleNames() {
        return sigmaRulesCache.keySet();
    }

    public String getRuleAsYaml(String ruleName) {
        return ((ParsedSigmaRule)this.sigmaRulesCache.get(ruleName)).toString();
    }

    public Cache<String, ParsedSigmaRule> getRules() {
        return this.sigmaRulesCache;
    }

    public List<ParsedSigmaRule> getRulesList() {
        return new ArrayList<ParsedSigmaRule>(this.sigmaRulesCache.values());
    }

    @Override
    public void handleUpdate(String key, ParsedSigmaRule value, ParsedSigmaRule oldValue,
        TopicPartition tp, long offset, long timestamp) {
        if (!value.equals(oldValue)) {
            if (observer != null) {
                observer.handleRuleUpdate(key, value, oldValue);
            }
        }
    }
}
