package io.confluent.sigmarules.streams;

import io.confluent.sigmarules.models.AggregateInfo;
import io.confluent.sigmarules.rules.SigmaRuleManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AggregateStreamsFactory {
    private Map<String, AggregateStream> streams = new HashMap<>();
    private Properties properties;

    public AggregateStreamsFactory(Properties properties) {
        this.properties = properties;
    }

    public void createAggregateStream(String streamName, SigmaRuleManager ruleManager) {
        if(streams.containsKey(streamName) == false) {
            String inputTopic = properties.getProperty("data.topic");
            String outputTopic = properties.getProperty("output.topic");

            inputTopic += "-agg-" + streamName.hashCode();
            AggregateInfo aggregateInfo = new AggregateInfo();
            aggregateInfo.setStreamName(inputTopic);
            aggregateInfo.setRuleManager(ruleManager);
            aggregateInfo.setConditionStatement(ruleManager.getConditions().getAggregateCondition());
            aggregateInfo.setInputTopic(inputTopic);
            aggregateInfo.setOutputTopic(outputTopic);
            aggregateInfo.setWindowTimeMS(ruleManager.getWindowTimeMS());

            AggregateStream newStream = new AggregateStream(properties, aggregateInfo);
            newStream.startStream();
            streams.put(streamName, newStream);
        }
    }
}
