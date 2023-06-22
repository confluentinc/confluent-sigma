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

package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.models.SigmaRule;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class AggregateResults {
    private SigmaRule rule = new SigmaRule();
    private JsonNode sourceData;
    private Long count = 0L;

    public SigmaRule getRule() {
        return rule;
    }

    public void setRule(SigmaRule rule) {
        this.rule = rule;
    }

    public JsonNode getSourceData() {
        return sourceData;
    }

    public void setSourceData(JsonNode sourceData) {
        this.sourceData = sourceData;
    }

    public Long getCount() { return this.count; }

    public void setCount(Long count) { this.count = count; }

    public static Serde<AggregateResults> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", AggregateResults.class);
        final Serializer<AggregateResults> aggregateSer = new KafkaJsonSerializer<>();
        aggregateSer.configure(serdeProps, false);

        final Deserializer<AggregateResults> aggregateDes = new KafkaJsonDeserializer<>();
        aggregateDes.configure(serdeProps, false);
        return Serdes.serdeFrom(aggregateSer, aggregateDes);
    }

}
