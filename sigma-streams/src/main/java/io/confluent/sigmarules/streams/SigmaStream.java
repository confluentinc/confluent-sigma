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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import io.confluent.sigmarules.appState.SigmaAppInstanceStore;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRuleFactoryObserver;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.aggregate.AggregateTopology;
import io.confluent.sigmarules.streams.simple.SimpleTopology;
import java.util.*;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaStream extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStream.class);
    final static String instanceId =  UUID.randomUUID().toString();

    private KafkaStreams streams;
    private SigmaRulesFactory ruleFactory;
    private SigmaAppInstanceStore instanceStore;
    private final Configuration jsonPathConf = createJsonPathConfig();

    public SigmaStream(Properties properties, SigmaRulesFactory ruleFactory) {
        super(properties);

        this.ruleFactory = ruleFactory;
        this.instanceStore = new SigmaAppInstanceStore(properties,this);

        // if the new or updated rule has an aggregate condition, we must either add a new
        // substream (for a new rule) or restart the topology if a rule has been changed
        // substream (for a new rule) or restart the topology if a rule has been changed
        // FF has been entered for dynamic changes to substreams
        ruleFactory.addObserver(new SigmaRuleFactoryObserver() {
            @Override
            public void processRuleUpdate(SigmaRule newRule, SigmaRule oldRule, Boolean newRuleAdded) {
                if (newRule.getConditionsManager().hasAggregateCondition()) {
                    if (newRuleAdded) {
                        logger.info("New aggregate rule: " + newRule.getTitle());
                        streams.close();
                        startStream();
                    } else {
                        // we only need to restart the topology if the window time has changed
                        if (newRule.getDetectionsManager().getWindowTimeMS().equals(
                            oldRule.getDetectionsManager().getWindowTimeMS()) == false) {
                            logger.info(newRule.getTitle() +
                                " window time has been modified. Restarting topology");
                            streams.close();
                            startStream();
                        }
                    }
                }
            }
        }, false);
    }

    public void startStream() {
        createTopic(inputTopic);
        createTopic(outputTopic);

        Topology topology = createTopology();
        streams = new KafkaStreams(topology, getStreamProperties());
        instanceStore.register();

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void stopStream() {
        this.streams.close();
    }

    // iterates through each rule and publishes to output topic for
    // each rule that is a match
    public Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
            
        // simple rules
        SimpleTopology simpleTopology = new SimpleTopology(this, ruleFactory, jsonPathConf);
        simpleTopology.createSimpleTopology(builder);

        // aggregate rules
        AggregateTopology aggregateTopology = new AggregateTopology(this, ruleFactory, jsonPathConf);
        aggregateTopology.createTopology(builder);

        return builder.build();
    }

    public KafkaStreams getStreams() {
        return streams;
    }

    public String getInstanceId() { return instanceId; }

    public SigmaRulesFactory getRuleFactory() {
        return ruleFactory;
    }

    public static Configuration createJsonPathConfig() {
        return Configuration.builder()
                .mappingProvider(new JacksonMappingProvider()) // Required for JsonNode object
                .jsonProvider(new JacksonJsonProvider()) // Required for JsonNode object
                .options(Option.SUPPRESS_EXCEPTIONS) // Return null when path is not found - https://github.com/json-path/JsonPath#tweaking-configuration
                .build();
    }

}
