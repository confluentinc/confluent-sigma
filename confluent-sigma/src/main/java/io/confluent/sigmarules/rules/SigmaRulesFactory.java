package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.models.SigmaRule;
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

    private Map<String, SigmaRuleManager> sigmaRules = new HashMap<>();
    private String fieldMapFile = null;
    private Set<String> filterList = new HashSet<>();
    private SigmaRulesStore sigmaRulesStore;
    private String product = null;
    private String service = null;
    private SigmaRuleFactoryObserver observer = null;
    private Properties properties;

    public SigmaRulesFactory(Properties properties) {
        initialize(properties);
    }

    private void initialize(Properties properties) {
        // create the rules cache
        this.properties = properties;

        try {
            this.fieldMapFile = properties.getProperty("field.mapping.file");
        } catch (IllegalArgumentException e) {
            logger.info("no field mapping file provided");
        }

        this.sigmaRulesStore = new SigmaRulesStore(properties);
        this.sigmaRulesStore.addObserver(this);
        this.checkSigmaRules();
        this.loadRules();
    }


    private void checkSigmaRules() {
        // single title
        if (properties.containsKey("sigma.rule.filter.title")) {
            String title = properties.getProperty("sigma.rule.filter.title");
            this.addTitleToFilterList(title);
        }

        // file containing a list of titles
        if (properties.containsKey("sigma.rule.filter.list")) {
            List<String> titles;
            try (Stream<String> lines = Files.lines(Paths.get(properties.getProperty("sigma.rule.filter.list")))) {
                titles = lines.collect(Collectors.toList());

                for(String title : titles) {
                    this.addTitleToFilterList(title);
                }
            } catch (IOException e) {
                logger.error("error reading filter list");
                e.printStackTrace();
            }
        }

        String product = null;
        String service = null;
        if (properties.containsKey("sigma.rule.filter.product")) {
            product = properties.getProperty("sigma.rule.filter.product");
        }

        if (properties.containsKey("sigma.rule.filter.service")) {
            service = properties.getProperty("sigma.rule.filter.service");
        }

        if (product != null || service != null) {
            this.setProductAndService(product, service);
        }
    }

    public void addObserver(SigmaRuleFactoryObserver observer, Boolean immediateCallback) {
        this.observer = observer;

        if (immediateCallback) {
            for (Map.Entry<String, SigmaRuleManager> sigmaRule : sigmaRules.entrySet()) {
                observer.handleNewRule(sigmaRule.getValue());
            }
        }
    }

    public void loadRules() {
        this.sigmaRulesStore.getRules().forEach((title, rule) -> {
           try {
                loadSigmaRule(title, rule.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidSigmaRuleException e) {
               e.printStackTrace();
           }
        });
    }

    public void loadSigmaRule(String title, String rule) throws IOException, InvalidSigmaRuleException {
        if (isFilteredRule(title)) {
            logger.info(title + " will not be loaded.  It does not match the filtered rules " +
                    "condition.");
            return;
        }

        Boolean newRule = false;
        SigmaRuleManager ruleManager = null;
        if (sigmaRules.containsKey(title)) {
            logger.info("Updating Sigma Rule: " + title);
            ruleManager = sigmaRules.get(title);

        } else {
            logger.info("Adding Sigma Rule: " + title);
            ruleManager = new SigmaRuleManager(this.fieldMapFile);
            sigmaRules.put(title, ruleManager);
            newRule = true;
        }
        ruleManager.loadSigmaRule(rule);

        if (newRule && observer != null) {
            observer.handleNewRule(ruleManager);
        }
    }

    /**
     * Should this rule be filtered out?  There is a combination of factors to determine if a rule should be used
     * like the product and service of the rule and potentially a list of specified titles.  Could be other ways in the
     * future.
     * @param title Of the rule to check
     * @return
     */
    protected boolean isFilteredRule(String title) {
        // verify product and service match before continuing
        SigmaRule rule = sigmaRulesStore.getRule(title);
        if (rule == null) throw new InvalidRulesException("Rule doesn't exist.");

        if (filterList.isEmpty()) {
            if (productAndServiceMatch(rule) == true) {
                return false;
            }
        } else if (filterList.contains(title)) {
            if (productAndServiceMatch(rule) == true) {
                return false;
            }
        }
        return true;
    }

    public Map<String, SigmaRuleManager> getSigmaRules() {
        return sigmaRules;
    }

    public String filterDetections(JsonNode data) {
        logger.debug("Total number of rules checking: " + sigmaRules.size());
        for (Map.Entry<String, SigmaRuleManager> sigmaRule : sigmaRules.entrySet()) {
            logger.info("Checking rule " + sigmaRule.getKey());
            if (sigmaRule.getValue().filterDetections(data) == true) {
                logger.info("Found match for " + sigmaRule.getKey());
                return sigmaRule.getKey();
            }
        } 

        return null;
    }

    /**
     * Filtered titles should be added prior to rules being loaded.
     * @param title title of rule to add to the filter list
     */
    public void addTitleToFilterList(String title) {
        filterList.add(title);
    }

    @Override
    public void handleRuleUpdate(String title, String rule) {
        try {
            loadSigmaRule(title, rule);
        } catch (IOException | InvalidSigmaRuleException e) {
            e.printStackTrace();
        }
    }

    public SigmaRuleManager getSigmaRuleManager(String title) { return this.sigmaRules.get(title); }

    public SigmaRule getRule(String title) {
        return this.sigmaRulesStore.getRule(title);
    }

    public String getRuleAsYaml(String title) {
        return this.sigmaRulesStore.getRuleAsYaml(title);
    }

    public void setProductAndService(String product, String service) {
        this.product = product;
        this.service = service;
    }

    /**
     * Check to see whether there is a product and service specified and if there is whether the SigmaRule matches.
     * If a product and service is not specified then any value is considred valid.
     * @param rule The SigmaRule to check
     * @return whether the SigmaRule matches the provided product and service.  Default to true if not specified
     */
    private Boolean productAndServiceMatch(SigmaRule rule) {
        logger.info("checking product: " + product + " service: " + service);
        logger.info("sigma rule product: " + rule.getLogsource().getProduct() + " service: " + rule.getLogsource().getService());
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

    private class InvalidRulesException extends RuntimeException {
        public InvalidRulesException(String s) {
            super(s);
        }
    }
}
