package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;

import java.sql.Timestamp;

public class SigmaBaseTopology {
    protected DetectionResults buildResults(SigmaRule rule, JsonNode sourceData) {
        DetectionResults results = new DetectionResults();
        results.setSourceData(sourceData);

        // check rule factory conditions manager for aggregate condition
        // and set it metadata
        if (rule != null) {
            results.getSigmaMetaData().setId(rule.getId());
            results.getSigmaMetaData().setTitle(rule.getTitle());
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        results.setTimeStamp(timestamp.getTime());

        return results;
    }
}
