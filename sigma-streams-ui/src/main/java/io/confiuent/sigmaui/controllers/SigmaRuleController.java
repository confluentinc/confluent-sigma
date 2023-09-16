/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package io.confiuent.sigmaui.controllers;

import io.confiuent.sigmaui.config.SigmaUIProperties;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.parsers.ParsedSigmaRule;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import io.confluent.sigmarules.rules.SigmaRulesStore;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SigmaRuleController {
    @Autowired
    SigmaUIProperties properties;

    private SigmaRulesStore rulesStore;
    private SigmaRuleParser parser = new SigmaRuleParser();


    @PostConstruct
    private void initialize() {
        rulesStore = new SigmaRulesStore(properties.getProperties());
    }

    @GetMapping({"/sigmaTitles"})
    public Set<String> getSigmaTitles() {
        return rulesStore.getRuleNames();
    }

    @GetMapping({"/sigmaRules"})
    public List<ParsedSigmaRule> getSigmaRules() {
        return rulesStore.getRulesList();
    }

    @GetMapping({"sigmaRule/{ruleTitle}"})
    public String getSigmaRule(@PathVariable String ruleTitle) {
        return rulesStore.getRuleAsYaml(ruleTitle);
    }

    @PostMapping({"addSigmaRule"})
    public void addSigmaRule(@RequestBody String rule) {
        System.out.println("addSigmaRule: " + rule);
        try {
            SigmaRule sigmaRule = parser.parseRule(rule);
            String key = sigmaRule.getTitle();
            System.out.println("Adding sigma rule: " + key);
            rulesStore.addRule(rule);
        } catch (IOException | InvalidSigmaRuleException | SigmaRuleParserException e) {
            e.printStackTrace();
        }
    }
}
