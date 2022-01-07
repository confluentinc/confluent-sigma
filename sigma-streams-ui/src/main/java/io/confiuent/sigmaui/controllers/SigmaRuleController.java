package io.confiuent.sigmaui.controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Set;

import io.confiuent.sigmaui.models.SigmaRule;
import io.confiuent.sigmaui.rules.SigmaRulesStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SigmaRuleController {
    @Autowired
    private SigmaRulesStore rules;

    private ObjectMapper mapper = new ObjectMapper((JsonFactory)new YAMLFactory());

    @GetMapping({"/sigmaTitles"})
    public Set<String> getSigmaTitles() {
        return this.rules.getRuleNames();
    }

    @GetMapping({"sigmaRule/{ruleTitle}"})
    public String getSigmaRule(@PathVariable String ruleTitle) {
        return this.rules.getRule(ruleTitle).toString();
    }

    @PostMapping({"addSigmaRule"})
    public void addSigmaRule(@RequestBody String rule) {
        System.out.println("addSigmaRule: " + rule);
        try {
            SigmaRule sigmaRule = (SigmaRule)this.mapper.readValue(rule, SigmaRule.class);
            String key = sigmaRule.getTitle();
            System.out.println("Adding sigma rule: " + key);
            this.rules.addRule(key, rule);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
