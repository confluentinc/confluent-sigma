package io.confluent.sigmarules.models;

import io.confluent.sigmarules.rules.SigmaRuleManager;

public class SigmaRulePredicate {
    private SigmaRuleManager rule = null;

    public SigmaRuleManager getRule() {
        return rule;
    }

    public void setRule(SigmaRuleManager rule) {
        this.rule = rule;
    }
}
