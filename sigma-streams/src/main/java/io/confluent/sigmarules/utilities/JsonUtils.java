package io.confluent.sigmarules.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.models.DetectionResults;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object payload) {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static JsonNode toJsonNode(Object payload) {
        return mapper.valueToTree(payload);
    }

    public static Serde<JsonNode> getJsonSerde(){
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", JsonNode.class);
        final Serializer<JsonNode> jsonSer = new KafkaJsonSerializer<>();
        jsonSer.configure(serdeProps, false);

        final Deserializer<JsonNode> jsonDes = new KafkaJsonDeserializer<>();
        jsonDes.configure(serdeProps, false);
        return Serdes.serdeFrom(jsonSer, jsonDes);
    }
}
