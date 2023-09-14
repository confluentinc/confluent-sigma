package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaDetection;
import io.confluent.sigmarules.models.SigmaDetections;
import io.confluent.sigmarules.models.SigmaRule;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.streams.processor.TopicNameExtractor;

public class SigmaBaseTopology {

    private String defaultOutputTopic;

    public void setDefaultOutputTopic(String defaultOutputTopic) {
        this.defaultOutputTopic = defaultOutputTopic;
    }

    protected DetectionResults buildResults(SigmaRule rule, JsonNode sourceData) {
        DetectionResults results = new DetectionResults();
        results.setSourceData(sourceData);
        Map<String, String> customFields = new HashMap<>();

        // check rule factory conditions manager for aggregate condition
        // and set it metadata
        if (rule != null) {
            results.getSigmaMetaData().setId(rule.getId());
            results.getSigmaMetaData().setTitle(rule.getTitle());
            results.getSigmaMetaData().setOutputTopic(defaultOutputTopic);

            // if this is a regular expression, add the group fields
            for (Map.Entry<String,SigmaDetections> detections : rule.getDetectionsManager().getAllDetections().entrySet()) {
                for (SigmaDetection detection : detections.getValue().getDetections()) {
                    for (Map.Entry<String,String> field : detection.getRegexMappedFields().entrySet()) {
                        customFields.put(field.getKey(), field.getValue());
                    }
                }
            }
            
            // overwrite the output topic if it is set in the rule
            // add any custom fields
            if (rule.getKafkaRule() != null) {
                if (rule.getKafkaRule().getOutputTopic() != null) {
                    results.getSigmaMetaData().setOutputTopic(rule.getKafkaRule().getOutputTopic());
                }

                for (Map.Entry<String,String> field : rule.getKafkaRule().getCustomFields().entrySet()) {
                    customFields.put(field.getKey(), field.getValue());
                }
            }

            if (customFields.size() > 0) {
                results.getSigmaMetaData().setCustomFields(customFields);
            }
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        results.setTimeStamp(timestamp.getTime());

        return results;
    }

    final TopicNameExtractor<String, DetectionResults> detectionTopicNameExtractor =
        (key, results, recordContext) -> results.getSigmaMetaData().getOutputTopic();
}
