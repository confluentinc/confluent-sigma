/*
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
 *
 */

package io.confluent.sigmarules.streams.simple;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.processor.SigmaProcessorManager;
import io.confluent.sigmarules.processor.SinkProcessor;
import io.confluent.sigmarules.processor.SourceProcessor;
import io.confluent.sigmarules.rules.SigmaRuleCheck;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaBaseTopology;
import io.confluent.sigmarules.streams.StreamManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleTopology extends SigmaBaseTopology {
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    final static Logger logger = LogManager.getLogger(SimpleTopology.class);
    private long matches = 0;
    private long lastCallTime = 0;
  
    private SigmaRuleCheck ruleCheck = new SigmaRuleCheck();
    private StreamManager streamManager;
    private SigmaRulesFactory ruleFactory;
    private Configuration jsonPathConf;
  
    public SimpleTopology(StreamManager streamManager, SigmaRulesFactory ruleFactory, 
    Configuration jsonPathConf) {
      this.streamManager = streamManager;
      this.ruleFactory = ruleFactory;
      this.jsonPathConf = jsonPathConf;
  
      setDefaultOutputTopic(streamManager.getOutputTopic());
    }
  
    public void createSimpleTopology(StreamsBuilder builder) {
      SigmaProcessorManager processorManager = new SigmaProcessorManager(streamManager);

      // add the source processor
      KStream<String, JsonNode> sigmaStream = processorManager.addSourceProcessor(builder);

      // process the data as JSON
      KStream<String, DetectionResults> detectionStream = 
        sigmaStream.flatMapValues(sourceData -> processSourceData(sourceData));

      // add the sink processor
      processorManager.addSinkProcessor(detectionStream);
  
    }

    private List<DetectionResults> processSourceData(JsonNode sourceData) {
      List<DetectionResults> results = new ArrayList<>();
  
      // iterate through all of the rules for this processor
      for (Map.Entry<String, SigmaRule> entry : ruleFactory.getSigmaRules().entrySet()) {
        SigmaRule rule = entry.getValue();
  
        if (false == rule.getConditionsManager().hasAggregateCondition()) {
          logger.debug("check rule " + rule.getTitle());
          
          // metric for records processed
          streamManager.setRecordsProcessed(streamManager.getRecordsProcessed() + 1);
  
          // validate the stream data against the rule
          if (ruleCheck.isValid(rule, sourceData, jsonPathConf)) {
            results.add(buildResults(rule, sourceData));
  
            if (logger.getLevel().isLessSpecificThan(Level.WARN)) {
                matches++;
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCallTime > MINUTE_IN_MILLIS) {
                    lastCallTime = currentTime;
                    logger.log(Level.INFO, "Number of matches " + matches);
                }
            }
  
            // metric for rule matches
            streamManager.setNumMatches(streamManager.getNumMatches() + 1);
  
            // return on first match or add all matches found
            if (streamManager.getFirstMatch())
              break;
          }
        }
      }
  
      return results;
    }
    
}
  