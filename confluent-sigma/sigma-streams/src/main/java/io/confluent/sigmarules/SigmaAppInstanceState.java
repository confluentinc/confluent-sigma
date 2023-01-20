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

package io.confluent.sigmarules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaAppInstanceState {

    private String applicationId;
    private String kafkaStreamsState;
    private List<String> hosts;
    private Integer numRules;
    private String sigmaRulesTopic;
    private String inputTopics;
    private String outputTopic;
    /**
     * Get a key to use that uniquely identifies this sigma app instance state.
     * @return unique key
     */
    public String getKey()
    {
        return getApplicationId();
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getNumRules() {
        return numRules;
    }

    public String getKafkaStreamsState() {
        return kafkaStreamsState;
    }

    public void setKafkaStreamsState(String kafkaStreamsState) {
        this.kafkaStreamsState = kafkaStreamsState;
    }

    public void setNumRules(Integer numRules) {
        this.numRules = numRules;
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

    public static Serde<SigmaAppInstanceState> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", SigmaAppInstanceState.class);
        final Serializer<SigmaAppInstanceState> sigmaRuleSer = new KafkaJsonSerializer<>();
        sigmaRuleSer.configure(serdeProps, false);

        final Deserializer<SigmaAppInstanceState> sigmaRuleDes = new KafkaJsonDeserializer<>();
        sigmaRuleDes.configure(serdeProps, false);
        return Serdes.serdeFrom(sigmaRuleSer, sigmaRuleDes);
    }

}
