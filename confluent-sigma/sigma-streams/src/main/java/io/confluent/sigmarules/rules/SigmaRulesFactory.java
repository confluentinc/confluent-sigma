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

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.fieldmapping.FieldMapper;
import io.confluent.sigmarules.models.LogSource;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.parsers.ParsedSigmaRule;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import io.confluent.sigmarules.streams.StreamManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaRulesFactory implements SigmaRuleObserver {
    final static Logger logger = LogManager.getLogger(SigmaRulesFactory.class.getName());

    private Map<String, SigmaRule> sigmaRules = new HashMap<>();
    private SigmaRulesStore sigmaRulesStore;
    private SigmaRuleParser rulesParser;
    private Properties properties;
    private StreamManager streamManager;

    // filters for specific rules
    private Set<String> titles = new HashSet<>();
    private String product = null;
    private String service = null;


    // This should only be called for testing as it does not load the rules store
    public SigmaRulesFactory() { rulesParser = new SigmaRuleParser(); }

    public SigmaRulesFactory(Properties properties) {
        initialize(properties);
    }

    private void initialize(Properties properties) {
        this.properties = properties;

        FieldMapper fieldMapFile = null;
        try {
            if (properties.containsKey("field.mapping.file"))
                fieldMapFile = new FieldMapper(properties.getProperty("field.mapping.file"));
        } catch (IllegalArgumentException | IOException e) {
            logger.info("no field mapping file provided");
        }

        streamManager = new StreamManager(properties);
        rulesParser = new SigmaRuleParser(fieldMapFile);

        // create the rules cache
        sigmaRulesStore = new SigmaRulesStore(properties);
        sigmaRulesStore.addObserver(this);

        // set the filters from the properties file
        setFiltersFromProperties(properties);

        // load the rules that apply to this processor
        getRulesfromStore();
    }

    public void setFiltersFromProperties(Properties properties) {
        // single title
        if (properties.containsKey("sigma.rule.filter.title")) {
            titles.add(properties.getProperty("sigma.rule.filter.title"));
        }

        // file containing a list of titles
        if (properties.containsKey("sigma.rule.filter.list")) {
            List<String> parsedTitles;
            try (Stream<String> lines = Files.lines(Paths.get(properties.getProperty("sigma.rule.filter.list")))) {
                parsedTitles = lines.collect(Collectors.toList());
                titles.addAll(parsedTitles);
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

    /**
     * Handles any new rules that are added to the Sigma Rules topic
     * @param title of the rule
     * @param rule as a string
     */
    // callback from kcache
    @Override
    public void handleRuleUpdate(String title, ParsedSigmaRule rule) {
        try {
            addRule(title, rulesParser.parseRule(rule));
        } catch (IOException | InvalidSigmaRuleException | SigmaRuleParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pulls in all rules that is currently stored in the Sigma Rules topic
     */
    private void getRulesfromStore() {
        List<String> kafkaOutputTopics = new ArrayList<>();

        this.sigmaRulesStore.getRules().forEach((title, rule) -> {
           try {
                addRule(title, rulesParser.parseRule(rule));

            } catch (IOException | InvalidSigmaRuleException | SigmaRuleParserException e) {
               logger.error("Exception thrown for rule: " + title + " rule: " + rule);
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks the rule to see if it matches the filters defined. If it matches, a rule will
     * get parsed and a SigmaRule object will be created.
     * @param title of the rule
     * @param sigmaRule as a string
     */
    public SigmaRule addRule(String title, SigmaRule sigmaRule)
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {
        //SigmaRule sigmaRule = rulesParser.parseRule(rule);

        if (shouldBeFiltered(sigmaRule)) {
            logger.info(title + " will not be loaded.  It does not match the filtered rules " +
                    "condition.");
            return null;
        }

        sigmaRules.put(title, sigmaRule);

        if (streamManager != null &&
            sigmaRule.getKafkaRule() != null &&
            sigmaRule.getKafkaRule().getOutputTopic() != null) {
            streamManager.createTopic(sigmaRule.getKafkaRule().getOutputTopic());
        }

        return sigmaRule;
    }

    /**
     * Should this rule be filtered out?  There is a combination of factors to determine if a
     * rule should be used like the product and service of the rule and potentially a list of
     * specified titles.  Could be other ways in the future.
     * @param sigmaRule rule to check
     * @return true if the rule should be filtered out
     */
    public boolean shouldBeFiltered(SigmaRule sigmaRule) {
        // verify product and service match before continuing
        if (titles.isEmpty()) {
            return !productAndServiceMatch(sigmaRule);
        } else if (titles.contains(sigmaRule.getTitle())) {
            return !productAndServiceMatch(sigmaRule);
        }
        return true;
    }

    public boolean isRuleFiltered(String title) {
      return !sigmaRules.containsKey(title);
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
        String product = null, service = null;

        LogSource logsource = rule.getLogsource();
        if (logsource != null) {
            product = logsource.getProduct();
            service = logsource.getService();
        }

        logger.info("sigma rule product: " + product + " service: " + service);

        boolean validProduct = true;
        boolean validService = true;

        if (this.product != null)
            if (!this.product.equals(product))
                validProduct = false;

        if (this.service != null)
            if (!this.service.equals(service))
                validService = false;


        return validProduct & validService;
    }

    public Map<String, SigmaRule> getSigmaRules() {
        return sigmaRules;
    }

    public SigmaRule getRule(String title) { return sigmaRules.get(title); }

    public String getRuleAsYaml(String title) { return sigmaRulesStore.getRuleAsYaml(title); }

    private class InvalidRulesException extends RuntimeException {
        public InvalidRulesException(String s) {
            super(s);
        }
    }
}
