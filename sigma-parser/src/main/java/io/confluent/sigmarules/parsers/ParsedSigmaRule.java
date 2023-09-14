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

package io.confluent.sigmarules.parsers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.models.KafkaRule;
import io.confluent.sigmarules.models.LogSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedSigmaRule {
    private String title;
    private String description;
    private String id;
    private String author;
    private List<String> references;
    private LogSource logsource;
    private Map<String, Object> detection;
    private KafkaRule kafka;

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
    public Map<String, Object> getDetection() {
        return detection;
    }

    public void setDetection(Map<String, Object> detection) {
        this.detection = detection;
    }

    public LogSource getLogsource() {
        return logsource;
    }

    public void setLogsource(LogSource logsource) {
        this.logsource = logsource;
    }
    public KafkaRule getKafka() {
        return kafka;
    }

    public void setKafka(KafkaRule kafka) {
        this.kafka = kafka;
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

    public static Serde<ParsedSigmaRule> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", ParsedSigmaRule.class);
        final Serializer<ParsedSigmaRule> sigmaRuleSer = new KafkaJsonSerializer<>();
        sigmaRuleSer.configure(serdeProps, false);

        final Deserializer<ParsedSigmaRule> sigmaRuleDes = new KafkaJsonDeserializer<>();
        sigmaRuleDes.configure(serdeProps, false);
        return Serdes.serdeFrom(sigmaRuleSer, sigmaRuleDes);
    }

}
