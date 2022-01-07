package io.confiuent.sigmaui.controllers;

import io.confiuent.sigmaui.models.RegexRule;
import io.confiuent.sigmaui.rules.RegexRuleObserver;
import io.confiuent.sigmaui.rules.RegexRulesStore;
import io.confiuent.sigmaui.rules.SigmaRulesStore;
import io.kcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
public class RegexController implements RegexRuleObserver {
    @Autowired
    private RegexRulesStore rules;

    @PostMapping({"addRegexRule"})
    public void addRegexRule(@RequestBody RegexRule rule) {
        System.out.println("addRegexRule: " + rule.toString());
        this.rules.addRule(rule.getSourceType(), rule);
    }

    @PostMapping({"deleteRegexRule"})
    public void deleteRegexRule(@RequestBody String sourceType) {
        System.out.println("deleteRegexRule: " + sourceType);
        this.rules.removeRule(sourceType);
    }

    @GetMapping({"/regexRules"})
    public Cache<String, RegexRule> getRegexRules() {
        return this.rules.getRules();
    }

    @Override
    public void handleRuleUpdate(String title, RegexRule rule) {

    }
}
