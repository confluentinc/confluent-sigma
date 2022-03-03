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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.sigmarules.models.AggregateInfo;
import io.confluent.sigmarules.models.AggregateResults;
import io.confluent.sigmarules.models.AggregateValues;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.parsers.AggregateParser;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

public class AggregateStream extends StreamManager {
    final static Logger logger = LogManager.getLogger(AggregateStream.class);

    private KafkaStreams streams;
    private ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private AggregateInfo aggregateInfo;
    private AggregateValues aggregateValues;

    public AggregateStream(Properties properties, AggregateInfo aggregateInfo) {
        super(properties);

        // application id must be unique
        this.properties.put(StreamsConfig.APPLICATION_ID_CONFIG, aggregateInfo.getStreamName());
        this.properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        this.aggregateInfo = aggregateInfo;
        parseConditionStatement();
        createTopic(this.aggregateInfo.getInputTopic());
    }

    private void parseConditionStatement() {
        AggregateParser parser = new AggregateParser();
        this.aggregateValues = parser.parseCondition(this.aggregateInfo.getConditionStatement());
    }

    public void startStream() {
        this.streams = new KafkaStreams(createBuilder().build(), properties);

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void stopStream() {
        this.streams.close();
    }

    private StreamsBuilder createBuilder() {
        final Serde<DetectionResults> detectionSerde = DetectionResults.getJsonSerde();
        final Serde<AggregateResults> aggregateSerde = AggregateResults.getJsonSerde();
        String tableTopic = this.aggregateInfo.getInputTopic() + "-table";
        StreamsBuilder builder = new StreamsBuilder();
        Long windowTimeMS = this.aggregateInfo.getWindowTimeMS();

        logger.info("input topic: " + this.aggregateInfo.getInputTopic());
        logger.info("output topic: " + this.aggregateInfo.getOutputTopic());
        logger.info("tableTopic: " + tableTopic);
        logger.info("window time: " + windowTimeMS);

        KStream<String, DetectionResults> sigmaData = builder.stream(this.aggregateInfo.getInputTopic(),
                Consumed.with(Serdes.String(), detectionSerde));
        sigmaData.selectKey((k, v) -> updateKey(k, v))
                .groupByKey()
                .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMillis(windowTimeMS), Duration.ofMillis(windowTimeMS)))
                .aggregate(
                    () -> new AggregateResults(),
                    (key, source, totals) -> updateValues(source, totals),
                    Materialized.with(Serdes.String(), aggregateSerde)
                )
                .toStream()
                .filter((k, v) -> doStreamFiltering(v))
                .map((Windowed<String> key, AggregateResults value) -> new KeyValue<>("", value.getDetection()))
                .to(this.aggregateInfo.getOutputTopic(), Produced.with(Serdes.String(), detectionSerde));;

        return builder;
    }

    private String updateKey(String key, DetectionResults value) {
        JsonNode sourceData = value.getSourceData();
        String groupBy = this.aggregateValues.getGroupBy();
        if (groupBy == null || groupBy.isEmpty()) {
            return "Counter";
        } else if (sourceData.has(groupBy)) {
            return sourceData.get(groupBy).toString();
        } else {
            return null;
        }
    }

    private AggregateResults updateValues(DetectionResults source, AggregateResults balance) {
        AggregateResults results = new AggregateResults();
        results.setResults(balance.getResults());
        results.setDetection(source);

        Long newValue = 1L;
        String distinctValue = this.aggregateValues.getDistinctValue();
        if (distinctValue == null || distinctValue.isEmpty()) {
            distinctValue = "CurrentCount";
        }

        if (results.getResults().containsKey(distinctValue)) {
            newValue = results.getResults().get(distinctValue) + 1;
        }
        results.getResults().put(distinctValue, newValue);
        logger.info("Title: "+ source.getSigmaMetaData().getTitle() + " update " + distinctValue + " to " + newValue);

        return results;
    }

    private Boolean doStreamFiltering(AggregateResults tableValues) {
        Long operationValue = Long.parseLong(this.aggregateValues.getOperationValue());
        Boolean matchFound = false;

        for (Map.Entry<String, Long> results : tableValues.getResults().entrySet()) {
            switch (this.aggregateValues.getOperation()) {
                case AggregateParser.EQUALS:
                    if (results.getValue() == operationValue) {
                        matchFound = true;
                    }
                    break;
                case AggregateParser.GREATER_THAN:
                    if (results.getValue() > operationValue) {
                        matchFound = true;
                    }
                    break;
                case AggregateParser.GREATER_THAN_EQUAL:
                    if (results.getValue() >= operationValue) {
                        matchFound = true;
                    }
                    break;
                case AggregateParser.LESS_THAN:
                    if (results.getValue() < operationValue) {
                        matchFound = true;
                    }
                    break;
                case AggregateParser.LESS_THAN_EQUAL:
                    if (results.getValue() <= operationValue) {
                        matchFound = true;
                    }
                    break;
                default:
                    System.out.println("Unhandled operation");
                    break;
            }

            if (matchFound == true) {
                logger.info("********************");
                logger.info("Found a match for " + this.aggregateInfo.getRuleManager().getRuleTitle());
                logger.info("Key: " + results.getKey() + " Value: " + results.getValue() +
                        " Comp Value " + operationValue);
                return true;
            }
        }

        return false;
    }

}
