package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.models.SigmaRulePredicate;
import io.confluent.sigmarules.rules.SigmaRuleManager;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.utilities.JsonUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.Properties;

public class SigmaStream extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStream.class);

    private KafkaStreams streams;
    private SigmaRulesFactory ruleFactory;
    private ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private String matchedDetection = null;
    private String inputTopic;
    private String outputTopic;

    public SigmaStream(Properties properties, SigmaRulesFactory ruleFactory) {
        super(properties);

        this.ruleFactory = ruleFactory;
        this.outputTopic = properties.getProperty("output.topic");
        this.inputTopic = properties.getProperty("data.topic");
    }

    public void startStream(SigmaRulePredicate[] predicates) {
        createTopic(inputTopic);

        StreamsBuilder builder = createBuilder(predicates);
        this.streams = new KafkaStreams(builder.build(), getStreamProperties());

        this.streams.cleanUp();
        this.streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void stopStream() {
        this.streams.close();
    }

    private StreamsBuilder createBuilder(SigmaRulePredicate[] predicates) {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> sigmaData = builder.stream(inputTopic,
                Consumed.with(Serdes.String(), JsonUtils.getJsonSerde()));
        for (Integer i = 0; i < predicates.length; i++) {
            Integer iterator = i;
            sigmaData.filter((k, v) -> doFiltering(predicates[iterator].getRule(), v))
                    .mapValues(sourceData -> buildResults(sourceData))
                    .to(detectionTopicNameExtractor, Produced.with(Serdes.String(), DetectionResults.getJsonSerde()));
        }

        return builder;
    }

    private Boolean doFiltering(SigmaRuleManager rule, JsonNode sourceData) {
         if (rule != null) {
            if (rule.filterDetections(sourceData) == true) {
                matchedDetection = rule.getRuleTitle();
                logger.info("Found match for " + matchedDetection);
                return true;
            }
        }

        return false;
    }

    private DetectionResults buildResults(JsonNode sourceData) {
        DetectionResults results = new DetectionResults();
        results.setSourceData(sourceData);

        // check rule factory conditions manager for aggregate condition
        // and set it metadata
        if (this.matchedDetection != null) {
            SigmaRule rule = this.ruleFactory.getRule(this.matchedDetection);
            if (rule != null) {
                results.getSigmaMetaData().setId(rule.getId());
                results.getSigmaMetaData().setTitle(rule.getTitle());
            }
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        results.setTimeStamp(timestamp.getTime());

        return results;
    }

    final TopicNameExtractor<String, DetectionResults> detectionTopicNameExtractor = (key, results, recordContext) -> {
        String oTopic = outputTopic;
        SigmaRuleManager ruleManager = this.ruleFactory.getSigmaRuleManager(results.getSigmaMetaData().getTitle());
        if (ruleManager.getConditions().hasAggregateConditon()) {
            oTopic = inputTopic + "-agg-" + results.getSigmaMetaData().getTitle().hashCode();
            logger.info("***** Sending to an aggregator stream:" + results.toJSON());
        }

        return oTopic;
    };
}
