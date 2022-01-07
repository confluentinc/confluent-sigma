package io.confiuent.sigmaui.rules;

import io.confiuent.sigmaui.models.RegexRule;

public interface RegexRuleObserver {
    void handleRuleUpdate(String title, RegexRule rule);
}
