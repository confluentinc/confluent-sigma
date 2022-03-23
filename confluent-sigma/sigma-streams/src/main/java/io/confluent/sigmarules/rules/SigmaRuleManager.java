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

import com.fasterxml.jackson.core.JsonFactory;
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
import java.util.Map;

public class SigmaRuleManager {
    final static Logger logger = LogManager.getLogger(SigmaRuleManager.class);

    private String ruleTitle;
    private DetectionsManager detections;
    private ConditionsManager conditions;

    public SigmaRuleManager() {
        conditions = new ConditionsManager();
        detections = new DetectionsManager();
    }

    public SigmaRuleManager(String fieldMapperFile) throws IOException {
        conditions = new ConditionsManager();
        detections = new DetectionsManager(fieldMapperFile);
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
        detections.loadSigmaDetections(sigmaRule, conditions);

        printSigmaRule();
    }

    public void printSigmaRule() {
        System.out.println("\n*********");
        System.out.println("title: " + this.ruleTitle + "\n");
        detections.printDetectionsAndConditions();
        System.out.println("*********\n");
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
