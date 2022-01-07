package io.confiuent.sigmaui.models;

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
public class SigmaRule {
    private String title;
    private String description;
    private String id;
    private String author;
    private List<String> references;
    private LogSource logsource;
    private Map<String, Object> detection;

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

}
