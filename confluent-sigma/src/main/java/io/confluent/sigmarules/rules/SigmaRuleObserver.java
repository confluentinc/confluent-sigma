package io.confluent.sigmarules.rules;

interface SigmaRuleObserver {
    void handleRuleUpdate(String title, String rule);
}
