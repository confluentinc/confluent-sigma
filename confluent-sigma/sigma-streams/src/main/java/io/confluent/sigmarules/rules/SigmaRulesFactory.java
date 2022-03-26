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

package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SigmaRulesFactory implements SigmaRuleObserver {
    final static Logger logger = LogManager.getLogger(SigmaRulesFactory.class.getName());

    private Map<String, SigmaRule> sigmaRules = new HashMap<>();
    private String fieldMapFile = null;
    private SigmaRulesStore sigmaRulesStore;
    private SigmaRuleFactoryObserver observer = null;
    private Properties properties;

    // filters for specific rules
    private Set<String> titles = new HashSet<>();
    private String product = null;
    private String service = null;

    public SigmaRulesFactory(Properties properties) {
        initialize(properties);
    }

    private void initialize(Properties properties) {
        this.properties = properties;

        try {
            fieldMapFile = properties.getProperty("field.mapping.file");
        } catch (IllegalArgumentException e) {
            logger.info("no field mapping file provided");
        }

        // create the rules cache
        sigmaRulesStore = new SigmaRulesStore(properties);
        sigmaRulesStore.addObserver(this);

        // set the filters from the properties file
        setFiltersFromProperties();

        // load the rules that apply to this processor
        getRulesfromStore();
    }

    private void setFiltersFromProperties() {
        // single title
        if (properties.containsKey("sigma.rule.filter.title")) {
            titles.add(properties.getProperty("sigma.rule.filter.title"));
        }

        // file containing a list of titles
        if (properties.containsKey("sigma.rule.filter.list")) {
            List<String> parsedTitles;
            try (Stream<String> lines = Files.lines(Paths.get(properties.getProperty("sigma.rule.filter.list")))) {
                parsedTitles = lines.collect(Collectors.toList());

                for(String title : parsedTitles) {
                    titles.add(title);
                }
            } catch (IOException e) {
                logger.error("error reading filter list");
                e.printStackTrace();
            }
        }

        if (properties.containsKey("sigma.rule.filter.product")) {
            product = properties.getProperty("sigma.rule.filter.product");
        }

        if (properties.containsKey("sigma.rule.filter.service")) {
            service = properties.getProperty("sigma.rule.filter.service");
        }
    }

    public void addObserver(SigmaRuleFactoryObserver observer, Boolean immediateCallback) {
        this.observer = observer;

        if (immediateCallback) {
            for (Map.Entry<String, SigmaRule> sigmaRule : sigmaRules.entrySet()) {
                observer.handleNewRule(sigmaRule);
            }
        }
    }

    /**
     * Handles any new rules that are added to the Sigma Rules topic
     * @param title of the rule
     * @param rule as a string
     * @return
     */
    // callback from kcache
    @Override
    public void handleRuleUpdate(String title, String rule) {
        try {
            addRule(title, rule);
        } catch (IOException | InvalidSigmaRuleException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pulls in all rules that is currently stored in the Sigma Rules topic
     * @param
     * @return
     */
    private void getRulesfromStore() {
        this.sigmaRulesStore.getRules().forEach((title, rule) -> {
           try {
                addRule(title, rule.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidSigmaRuleException e) {
               e.printStackTrace();
           }
        });
    }

    /**
     * Checks the rule to see if it matches the filters defined. If it matches, a rule will
     * get parsed and a SigmaRule object will be created.
     * @param title of the rule
     * @param rule as a string
     * @return
     */
    public void addRule(String title, String rule) throws IOException, InvalidSigmaRuleException {
        if (isFilteredRule(title)) {
            logger.info(title + " will not be loaded.  It does not match the filtered rules " +
                    "condition.");
            return;
        }

        Boolean newRule = false;
        SigmaRule sigmaRule = null;
        if (sigmaRules.containsKey(title)) {
            logger.info("Updating Sigma Rule: " + title);
            sigmaRule = sigmaRules.get(title);

        } else {
            logger.info("Adding Sigma Rule: " + title);
            ruleManager = new SigmaRuleParser(this.fieldMapFile);
            sigmaRules.put(title, ruleManager);
            newRule = true;
        }
        ruleManager.loadSigmaRule(rule);

        if (newRule && observer != null) {
            observer.handleNewRule(ruleManager);
        }
    }

    /**
     * Should this rule be filtered out?  There is a combination of factors to determine if a
     * rule should be used like the product and service of the rule and potentially a list of
     * specified titles.  Could be other ways in the future.
     * @param title Of the rule to check
     * @return
     */
    public boolean isFilteredRule(String title) {
        // verify product and service match before continuing
        SigmaRule rule = sigmaRulesStore.getRule(title);
        if (rule == null) throw new InvalidRulesException("Rule doesn't exist.");

        if (titles.isEmpty()) {
            if (productAndServiceMatch(rule) == true) {
                return false;
            }
        } else if (titles.contains(title)) {
            if (productAndServiceMatch(rule) == true) {
                return false;
            }
        }
        return true;
    }


    /**
     * Check to see whether there is a product and service specified and if there is whether the
     * SigmaRule matches. If a product and service is not specified then any value is considered
     * valid.
     * @param rule The SigmaRule to check
     * @return whether the SigmaRule matches the provided product and service.  Default to true
     * if not specified
     */
    private Boolean productAndServiceMatch(SigmaRule rule) {
        logger.info("checking product: " + product + " service: " + service);
        logger.info("sigma rule product: " + rule.getLogsource().getProduct() + " service: " +
            rule.getLogsource().getService());
        Boolean validProduct = true;
        Boolean validService = true;

        if (product != null) {
            if (!product.equals(rule.getLogsource().getProduct())) {
                validProduct = false;
            }
        }

        if (service != null) {
            if (!service.equals(rule.getLogsource().getService())) {
                validService = false;
            }
        }

        return validProduct & validService;
    }

    public Map<String, SigmaRuleParser> getSigmaRules() {
        return sigmaRules;
    }

    public String filterDetections(JsonNode data) {
        logger.debug("Total number of rules checking: " + sigmaRules.size());
        for (Map.Entry<String, SigmaRuleParser> sigmaRule : sigmaRules.entrySet()) {
            logger.info("Checking rule " + sigmaRule.getKey());
            if (sigmaRule.getValue().filterDetections(data) == true) {
                logger.info("Found match for " + sigmaRule.getKey());
                return sigmaRule.getKey();
            }
        } 

        return null;
    }
    public SigmaRuleParser getSigmaRuleManager(String title) { return this.sigmaRules.get(title); }

    public SigmaRule getRule(String title) {
        return this.sigmaRulesStore.getRule(title);
    }

    public String getRuleAsYaml(String title) {
        return this.sigmaRulesStore.getRuleAsYaml(title);
    }

    private class InvalidRulesException extends RuntimeException {
        public InvalidRulesException(String s) {
            super(s);
        }
    }
}
