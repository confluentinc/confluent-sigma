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

package io.confluent.sigmarules.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import io.confluent.sigmarules.models.AggregateValues;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.parsers.AggregateParser;
import io.confluent.sigmarules.rules.SigmaRuleCheck;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SlidingWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AggregateTopology extends SigmaBaseTopology {
    final static Logger logger = LogManager.getLogger(AggregateTopology.class);

    private final SigmaRuleCheck ruleCheck = new SigmaRuleCheck();
    private SigmaRule currentRule = null;

    public void createAggregateTopology(StreamManager streamManager, KStream<String, JsonNode> sigmaStream,
        SigmaRulesFactory ruleFactory, String outputTopic, Configuration jsonPathConf) {

        final Serde<AggregateResults> aggregateSerde = AggregateResults.getJsonSerde();

        for (Map.Entry<String, SigmaRule> entry : ruleFactory.getSigmaRules().entrySet()) {
            SigmaRule rule = entry.getValue();
            if (rule.getConditionsManager().hasAggregateCondition()) {
                streamManager.setRecordsProcessed(streamManager.getRecordsProcessed() + 1);
                Long windowTimeMS = rule.getDetectionsManager().getWindowTimeMS();

                sigmaStream.flatMap((key, sourceData) -> {
                    List<KeyValue<String, AggregateResults>> results = new ArrayList<>();
                    logger.debug("check rule " + rule.getTitle());
                    SigmaRule currentRule = ruleFactory.getRule(rule.getTitle());
                    if (ruleCheck.isValid(currentRule, sourceData, jsonPathConf)) {
                        AggregateResults aggResults = new AggregateResults();
                        aggResults.setRule(currentRule);
                        aggResults.setSourceData(sourceData);
                        results.add(new KeyValue<>(updateKey(currentRule, sourceData), aggResults));
                    }
                    return results;
                })
                .selectKey((k, sourceData) -> updateKey(sourceData.getRule(),
                    sourceData.getSourceData()))
                .groupByKey(Grouped.with(
                    Serdes.String(), /* key */
                    aggregateSerde))
                .windowedBy(
                    SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMillis(windowTimeMS),
                        Duration.ofMillis(windowTimeMS)))
                .aggregate(
                    () -> new AggregateResults(),
                    (key, source, aggregate) -> {
                        aggregate.setRule(source.getRule());
                        aggregate.setSourceData(source.getSourceData());
                        aggregate.setCount(aggregate.getCount() + 1);
                        return aggregate;
                    },
                    Materialized.with(Serdes.String(), aggregateSerde)
                )
                .toStream()
                .filter((k, results) -> doStreamFiltering(results.getRule(), results))
                .map((key, value) -> {
                    streamManager.setNumMatches(streamManager.getNumMatches() + 1);
                    return new KeyValue<>("", buildResults(value.getRule(), value.getSourceData()));
                })
                .to(outputTopic,
                    Produced.with(Serdes.String(), DetectionResults.getJsonSerde()));

            }
        }
    }

    public AggregateResults updateValues(AggregateResults results) {
        results.setCount(results.getCount() + 1);
        return results;
    }

    /*
    public void createAggregateTopology(KStream<String, JsonNode> sigmaStream, SigmaRule rule,
        String outputTopic, Configuration jsonPathConf) {

        final Serde<AggregateResults> aggregateSerde = AggregateResults.getJsonSerde();
        Long windowTimeMS = rule.getDetectionsManager().getWindowTimeMS();

        AggregateValues aggregateValues =
            rule.getConditionsManager().getAggregateCondition().getAggregateValues();

        sigmaStream.filter((k, sourceData) -> ruleCheck.isValid(rule, sourceData, jsonPathConf))
            .selectKey((k, sourceData) -> updateKey(rule, aggregateValues, sourceData))
            .groupByKey()
            .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMillis(windowTimeMS),
                Duration.ofMillis(windowTimeMS)))
            .aggregate(
                () -> new AggregateResults(),
                (key, source, aggregate) -> {
                    aggregate.setSourceData(source);
                    aggregate.setCount(aggregate.getCount() + 1);
                    return aggregate;
                },
                Materialized.with(Serdes.String(), aggregateSerde)
            )
            .toStream()
            .filter((k, results) -> doStreamFiltering(k.key(), rule, aggregateValues, results))
            .map((Windowed<String> key, AggregateResults value) ->
                new KeyValue<>("", buildResults(rule, value.getSourceData())))
            .to(outputTopic, Produced.with(Serdes.String(), DetectionResults.getJsonSerde()));
    }
*/

    private String updateKey(SigmaRule rule, JsonNode source) {
        // make the key the title + groupBy + distinctValue, so we have 1 unique stream
        AggregateValues aggregateValues = rule.getConditionsManager().getAggregateCondition()
            .getAggregateValues();

        String newKey = rule.getTitle();
        String groupBy = aggregateValues.getGroupBy();
        if (groupBy != null && groupBy.isEmpty() == false && source.get(groupBy) != null) {
            newKey = newKey + "-" + source.get(aggregateValues.getGroupBy()).asText();
        }

        String distinctValue = aggregateValues.getDistinctValue();
        if (distinctValue != null && distinctValue.isEmpty() == false &&
            source.get(distinctValue) != null) {
            newKey = newKey + "-" +  source.get(distinctValue).asText();
        }

        currentRule = rule;
        return newKey;
    }

    private Boolean doStreamFiltering(SigmaRule rule, AggregateResults results) {
        AggregateValues aggregateValues = rule.getConditionsManager().getAggregateCondition()
            .getAggregateValues();
        long operationValue = Long.parseLong(aggregateValues.getOperationValue());
        boolean matchFound = false;

        switch (aggregateValues.getOperation()) {
            case AggregateParser.EQUALS:
                if (results.getCount() == operationValue) {
                    matchFound = true;
                }
                break;
            case AggregateParser.GREATER_THAN:
                if (results.getCount() > operationValue) {
                    matchFound = true;
                }
                break;
            case AggregateParser.GREATER_THAN_EQUAL:
                if (results.getCount() >= operationValue) {
                    matchFound = true;
                }
                break;
            case AggregateParser.LESS_THAN:
                if (results.getCount() < operationValue) {
                    matchFound = true;
                }
                break;
            case AggregateParser.LESS_THAN_EQUAL:
                if (results.getCount() <= operationValue) {
                    matchFound = true;
                }
                break;
            default:
                logger.warn("Unhandled operation: " + aggregateValues.getOperation() +
                        ". For rule " + rule.getTitle());
                break;
        }

        if (matchFound) {
            logger.info("Found a match Title: " + rule.getTitle() +
                " (" + results.getCount() +
                " " + aggregateValues.getOperation() +
                " " + operationValue + ")");

            return true;
        }

        return false;
    }
}
