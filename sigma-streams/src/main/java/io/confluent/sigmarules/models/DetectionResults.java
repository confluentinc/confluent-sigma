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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

@JsonInclude(Include.NON_NULL)
public class DetectionResults {
    private Long timeStamp = 0L;
    @JsonUnwrapped
    private RuleResults sigmaMetaData = new RuleResults();
    private JsonNode sourceData;

    public RuleResults getSigmaMetaData() {
        return sigmaMetaData;
    }

    public void setSigmaMetaData(RuleResults sigmaMetaData) {
        this.sigmaMetaData = sigmaMetaData;
    }

    public JsonNode getSourceData() {
        return sourceData;
    }

    public void setSourceData(JsonNode sourceData) {
        this.sourceData = sourceData;
    }

   public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String toPrettyJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public static Serde<DetectionResults> getJsonSerde(){
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", DetectionResults.class);
        final Serializer<DetectionResults> detectionSer = new KafkaJsonSerializer<>();
        detectionSer.configure(serdeProps, false);

        final Deserializer<DetectionResults> detectionDes = new KafkaJsonDeserializer<>();
        detectionDes.configure(serdeProps, false);
        return Serdes.serdeFrom(detectionSer, detectionDes);
    }

}
