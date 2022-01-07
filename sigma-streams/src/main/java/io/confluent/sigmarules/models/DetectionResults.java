package io.confluent.sigmarules.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class DetectionResults {
    private Long timeStamp = 0L;
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
