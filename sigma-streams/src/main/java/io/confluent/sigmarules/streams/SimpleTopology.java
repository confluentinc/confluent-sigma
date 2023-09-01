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

package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRuleCheck;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleTopology extends SigmaBaseTopology {
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    final static Logger logger = LogManager.getLogger(SimpleTopology.class);

  private SigmaRuleCheck ruleCheck = new SigmaRuleCheck();
  private long matches = 0;
  private long lastCallTime = 0;

  public void createSimpleTopology(StreamManager streamManager, KStream<String, JsonNode> sigmaStream,
      SigmaRulesFactory ruleFactory, String outputTopic, Configuration jsonPathConf,
      Boolean firstMatch) {

    setDefaultOutputTopic(outputTopic);

    sigmaStream.flatMapValues(sourceData -> {
          List<DetectionResults> results = new ArrayList<>();
          for (Map.Entry<String, SigmaRule> entry : ruleFactory.getSigmaRules().entrySet()) {
            SigmaRule rule = entry.getValue();

            if (false == rule.getConditionsManager().hasAggregateCondition()) {
              logger.debug("check rule " + rule.getTitle());
              streamManager.setRecordsProcessed(streamManager.getRecordsProcessed() + 1);

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

                streamManager.setNumMatches(streamManager.getNumMatches() + 1);

                if (firstMatch)
                  break;
              }
            }
          }
          return results;
        })
        .to(detectionTopicNameExtractor, Produced.with(Serdes.String(), DetectionResults.getJsonSerde()));

  }

}
