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
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRuleCheck;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.utilities.JsonUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.Properties;

public class SigmaStream extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStream.class);

    private KafkaStreams streams;
    private SigmaRulesFactory ruleFactory;
    private ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private String inputTopic;
    private String outputTopic;
    private SigmaRuleCheck ruleCheck;

    public SigmaStream(Properties properties, SigmaRulesFactory ruleFactory) {
        super(properties);

        this.ruleFactory = ruleFactory;
        this.ruleCheck = new SigmaRuleCheck();
        this.outputTopic = properties.getProperty("output.topic");
        this.inputTopic = properties.getProperty("data.topic");
    }

    public void startStream(SigmaRulePredicate[] predicates) {
        createTopic(inputTopic);

        Topology topology = createTopology(predicates);
        streams = new KafkaStreams(topology, getStreamProperties());

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void stopStream() {
        this.streams.close();
    }

    public Topology createTopology(SigmaRulePredicate[] predicates) {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> sigmaStream = builder.stream(inputTopic,
                Consumed.with(Serdes.String(), JsonUtils.getJsonSerde()));
        for (Integer i = 0; i < predicates.length; i++) {
            Integer iterator = i;
            SigmaRule rule = predicates[iterator].getRule();

            if (rule.getConditionsManager().hasAggregateCondition()) {
                AggregateTopology aggregateTopology = new AggregateTopology();
                aggregateTopology.createAggregateTopology(sigmaStream, rule, outputTopic);
            } else {
                SimpleTopology simpleTopology = new SimpleTopology();
                simpleTopology.createSimpleTopology(sigmaStream, rule, outputTopic);
            }
        }

        return builder.build();
    }
}
