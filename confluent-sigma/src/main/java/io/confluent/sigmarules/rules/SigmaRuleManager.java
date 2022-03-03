package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.models.SigmaRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SigmaRuleManager {
    final static Logger logger = LogManager.getLogger(SigmaRuleManager.class);

    private DetectionsManager detections;
    private ConditionsManager conditions;
    private String ruleTitle;

    public SigmaRuleManager() {
        conditions = new ConditionsManager();
        detections = new DetectionsManager();
    }

    public SigmaRuleManager(String fieldMapperFile) throws IOException {
        conditions = new ConditionsManager();
        detections = new DetectionsManager();
    }

    public void loadSigmaRuleFile(String filename) throws IOException {
        String rule = Files.readString(Path.of(filename));
        try {
            loadSigmaRule(rule);
        } catch (InvalidSigmaRuleException e) {
            logger.warn("Unable to parse rule at filename = " + filename + ".");
            logger.warn(e);
        }
    }
            
    public void loadSigmaRule(String rule) throws IOException, InvalidSigmaRuleException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        SigmaRule sigmaRule = yamlMapper.readValue(rule, SigmaRule.class);
        this.ruleTitle = sigmaRule.getTitle();
    }

    public Boolean filterDetections(JsonNode data) {
        // Filter the Stream
        //logger.info("Checking conditions for: " + ruleTitle);
        return conditions.checkConditions(detections, data);
    }

    public DetectionsManager getDetections() {
        return this.detections;
    }

    public ConditionsManager getConditions() {
        return this.conditions;
    }

    public String getRuleTitle() { return this.ruleTitle; }

    public Long getWindowTimeMS() { return this.detections.getWindowTimeMS(); }
}
