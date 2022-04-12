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

package io.confluent.sigmarules.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.fieldmapping.FieldMapper;
import io.confluent.sigmarules.models.SigmaRule;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaRuleParser {
    final static Logger logger = LogManager.getLogger(SigmaRuleParser.class);
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private io.confluent.sigmarules.parsers.DetectionParser detectionParser;
    private io.confluent.sigmarules.parsers.ConditionParser conditionParser;

    public SigmaRuleParser() {
        detectionParser = new io.confluent.sigmarules.parsers.DetectionParser();
        conditionParser = new io.confluent.sigmarules.parsers.ConditionParser();
    }

    public SigmaRuleParser(FieldMapper fieldMapperFile) {
        detectionParser = new io.confluent.sigmarules.parsers.DetectionParser(fieldMapperFile);
        conditionParser = new io.confluent.sigmarules.parsers.ConditionParser();
    }

    public SigmaRule parseRule(String rule)
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {
        io.confluent.sigmarules.parsers.ParsedSigmaRule parsedSigmaRule = yamlMapper.readValue(rule, io.confluent.sigmarules.parsers.ParsedSigmaRule.class);

        SigmaRule sigmaRule = new SigmaRule();
        sigmaRule.copyParsedSigmaRule(parsedSigmaRule);

        sigmaRule.setDetection(detectionParser.parseDetections(parsedSigmaRule));
        sigmaRule.setConditionsManager(conditionParser.parseCondition(parsedSigmaRule));
        return sigmaRule;
    }

    /*
    public Boolean filterDetections(JsonNode data) {
        // Filter the Stream
        //logger.info("Checking conditions for: " + ruleTitle);
        return conditions.checkConditions(detections, data);
    }

     */
}
