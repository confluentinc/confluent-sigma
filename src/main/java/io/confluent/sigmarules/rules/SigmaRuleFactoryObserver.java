package io.confluent.sigmarules.rules;

public interface SigmaRuleFactoryObserver {
    void handleNewRule(SigmaRuleManager newRule);
}
