package io.confluent.sigmarules.models;

import io.confluent.sigmarules.rules.SigmaRuleManager;

public class AggregateInfo {
    private String streamName;
    private SigmaRuleManager ruleManager;
    private String conditionStatement;
    private String inputTopic;
    private String outputTopic;
    private Long windowTimeMS = 0L;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public SigmaRuleManager getRuleManager() {
        return ruleManager;
    }

    public void setRuleManager(SigmaRuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public String getConditionStatement() {
        return conditionStatement;
    }

    public void setConditionStatement(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    public Long getWindowTimeMS() {
        return windowTimeMS;
    }

    public void setWindowTimeMS(Long windowTimeMS) {
        this.windowTimeMS = windowTimeMS;
    }
}
