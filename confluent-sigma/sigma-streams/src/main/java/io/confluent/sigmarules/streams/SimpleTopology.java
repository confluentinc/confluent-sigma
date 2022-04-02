package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRuleCheck;
import java.sql.Timestamp;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleTopology {
  final static Logger logger = LogManager.getLogger(SimpleTopology.class);

  private SigmaRuleCheck ruleCheck = new SigmaRuleCheck();

  public void createSimpleTopology(KStream<String, JsonNode> sigmaStream, SigmaRule rule,
      String outputTopic) {
    sigmaStream.filter((k, sourceData) -> ruleCheck.isValid(rule, sourceData))
        .mapValues(sourceData -> buildResults(rule, sourceData))
        .to(outputTopic, Produced.with(Serdes.String(), DetectionResults.getJsonSerde()));

  }

  private DetectionResults buildResults(SigmaRule rule, JsonNode sourceData) {
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
