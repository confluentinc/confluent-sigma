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

import io.confiuent.sigmaui.models.RegexRule;
import io.confiuent.sigmaui.rules.RegexRuleObserver;
import io.confiuent.sigmaui.rules.RegexRulesStore;
import io.kcache.Cache;
import javax.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class RegexController implements RegexRuleObserver {
    private RegexRulesStore rules;

    @PostConstruct
    private void initialize() {

    }

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
