package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;

import java.sql.Timestamp;
import org.apache.kafka.streams.processor.TopicNameExtractor;

public class SigmaBaseTopology {

    private String defaultOutputTopic;

    public void setDefaultOutputTopic(String defaultOutputTopic) {
        this.defaultOutputTopic = defaultOutputTopic;
    }

    protected DetectionResults buildResults(SigmaRule rule, JsonNode sourceData) {
        DetectionResults results = new DetectionResults();
        results.setSourceData(sourceData);

        // check rule factory conditions manager for aggregate condition
        // and set it metadata
        if (rule != null) {
            results.getSigmaMetaData().setId(rule.getId());
            results.getSigmaMetaData().setTitle(rule.getTitle());
            results.getSigmaMetaData().setOutputTopic(defaultOutputTopic);

            //overwrite the output topic if it is set in the rule
            if (rule.getKafkaRule() != null) {
                if (rule.getKafkaRule().getOutputTopic() != null) {
                    results.getSigmaMetaData().setOutputTopic(rule.getKafkaRule().getOutputTopic());
                }
            }
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        results.setTimeStamp(timestamp.getTime());

        return results;
    }

    final TopicNameExtractor<String, DetectionResults> detectionTopicNameExtractor =
        (key, results, recordContext) -> results.getSigmaMetaData().getOutputTopic();
}
