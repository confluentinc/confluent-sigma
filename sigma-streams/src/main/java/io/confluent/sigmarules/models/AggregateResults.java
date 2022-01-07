package io.confluent.sigmarules.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class AggregateResults {
    private Map<String, Long> results = new HashMap<>();
    private DetectionResults detection = new DetectionResults();

    public Map<String, Long> getResults() {
        return results;
    }

    public void setResults(Map<String, Long> results) {
        this.results = results;
    }

    public DetectionResults getDetection() {
        return detection;
    }

    public void setDetection(DetectionResults detection) {
        this.detection = detection;
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
