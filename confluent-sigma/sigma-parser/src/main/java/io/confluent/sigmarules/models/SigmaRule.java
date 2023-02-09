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

package io.confluent.sigmarules.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.parsers.ParsedSigmaRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaRule {
    private String title;
    private String description;
    private String id;
    private String author;
    private List<String> references = new ArrayList<>();
    private LogSource logsource = new LogSource();
    private DetectionsManager detectionsManager = new DetectionsManager();
    private ConditionsManager conditionsManager = new ConditionsManager();

    private KafkaRule kafkaRule;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public DetectionsManager getDetectionsManager() { return detectionsManager; }

    public void setDetection(DetectionsManager detectionsManager) { this.detectionsManager = detectionsManager; }

    public ConditionsManager getConditionsManager() { return conditionsManager; }

    public void setConditionsManager(ConditionsManager conditionsManager) { this.conditionsManager = conditionsManager; }

    public LogSource getLogsource() {
        return logsource;
    }

    public void setLogsource(LogSource logsource) {
        this.logsource = logsource;
    }

    public KafkaRule getKafkaRule() {
        return kafkaRule;
    }

    public void setKafkaRule(KafkaRule kafkaRule) {
        this.kafkaRule = kafkaRule;
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static Serde<SigmaRule> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", SigmaRule.class);
        final Serializer<SigmaRule> sigmaRuleSer = new KafkaJsonSerializer<>();
        sigmaRuleSer.configure(serdeProps, false);

        final Deserializer<SigmaRule> sigmaRuleDes = new KafkaJsonDeserializer<>();
        sigmaRuleDes.configure(serdeProps, false);
        return Serdes.serdeFrom(sigmaRuleSer, sigmaRuleDes);
    }

    public void copyParsedSigmaRule(ParsedSigmaRule parsedSigmaRule) {
        this.title = parsedSigmaRule.getTitle();
        this.description = parsedSigmaRule.getDescription();
        this.id = parsedSigmaRule.getId();
        this.author = parsedSigmaRule.getAuthor();
        this.references = parsedSigmaRule.getReferences();
        this.logsource = parsedSigmaRule.getLogsource();
        this.kafkaRule = parsedSigmaRule.getKafka();
    }

}
