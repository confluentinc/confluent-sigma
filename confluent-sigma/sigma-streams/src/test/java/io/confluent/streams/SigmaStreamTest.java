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

package io.confluent.streams;

import io.confluent.sigmarules.streams.SigmaRulePredicate;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.tools.SigmaRuleLoader;
import io.confluent.sigmarules.utilities.SigmaOptions;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigmaStreamTest {
    private TopologyTestDriver td;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    private Topology topology;

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    @BeforeAll
    void setUp() {
        kafka.start();
        loadTestRules();
    }

    Properties getProperties() {
        Properties testProperties = new Properties();
        //TODO should be bootstrap.serverS -- use defined variable in place of string
        //testProperties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        testProperties.setProperty("bootstrap.server", kafka.getBootstrapServers());
        testProperties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        testProperties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty("sigma.rules.topic", "rules");
        testProperties.setProperty("schema.registry", "localhost:8888");

        return testProperties;
    }

    /**
     * Load the test rules for this unit test
     */
    private void loadTestRules() {
        SigmaOptions options = new SigmaOptions();
        options.setProperties(getProperties());

        SigmaRuleLoader sigma = new SigmaRuleLoader(options);
        sigma.loadSigmaDirectory("config/rules");
    }

    @AfterAll
    public void tearDown() {
        kafka.stop();
        td.close();
    }


//    @Test
//    public void shouldIncludeValueWithLengthGreaterThanFive() {
//        Properties testProperties = getProperties();
//        testProperties.setProperty("sigma.rule.filter.product", "zeek");
//
//        List<SigmaRuleParser> ruleList = new ArrayList<>();
//        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
//        srf.addObserver((rule -> ruleList.add(rule)), true);
//        System.out.println("number of rules " + ruleList.size());
//
//        SigmaRulePredicate[] predicates = new SigmaRulePredicate[ruleList.size()];
//        for(int i = 0; i < ruleList.size(); i++) {
//            predicates[i] = new SigmaRulePredicate();
//            predicates[i].setRule(ruleList.get(i));
//        }
//
//        //testProperties.setProperty("bootstrap.server", "foo:1234");
//
//        SigmaStream stream = new SigmaStream(testProperties, srf);
//        topology = stream.createTopology(predicates);
//        //topology = stream.retainWordsLongerThan5Letters();
//        System.out.println("about to create test driver");
//        td = new TopologyTestDriver(topology, testProperties);
//
//        inputTopic = td.createInputTopic("input-topic", Serdes.String().serializer(), Serdes.String().serializer());
//        outputTopic = td.createOutputTopic("output-topic", Serdes.String().deserializer(), Serdes.String().deserializer());
//
//        //inputTopic.pipeInput("foo", "barrrrr");
//        assertTrue(outputTopic.isEmpty());
//
//        /*
//        inputTopic.pipeInput("foo", "barrrrr");
//        assertThat(outputTopic.readValue(), equalTo("barrrrr"));
//        assertThat(outputTopic.isEmpty(), is(true));
//
//        inputTopic.pipeInput("foo", "bar");
//        assertThat(outputTopic.isEmpty(), is(true));
//
//         */
//    }
}
