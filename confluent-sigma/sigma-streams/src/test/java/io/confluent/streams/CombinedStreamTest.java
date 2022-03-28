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

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaRulePredicate;
import io.confluent.sigmarules.streams.SigmaStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CombinedStreamTest {
    final static Logger logger = LogManager.getLogger(CombinedStreamTest.class);

    private TopologyTestDriver td;
    private Topology topology;
    private SigmaRulePredicate[] predicates;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;
    private ObjectMapper objectMapper = new ObjectMapper();


    Properties getProperties() {
        Properties testProperties = new Properties();
        //TODO should be bootstrap.serverS -- use defined variable in place of string
        //testProperties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        testProperties.setProperty("bootstrap.server", "foo:1234");
        testProperties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        testProperties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty("sigma.rules.topic", "rules");
        testProperties.setProperty("schema.registry", "localhost:8888");
        testProperties.setProperty("output.topic", "test-output");
        testProperties.setProperty("data.topic", "test-input");

        return testProperties;
    }

    private void initializePredicates(SigmaRulesFactory srf) {
        // initialize the predicates
        Map<String, SigmaRule> rulesList = srf.getSigmaRules();
        logger.info("number of rules " + rulesList.size());

        Integer i = 0;
        predicates = new SigmaRulePredicate[rulesList.size()];
        for (Map.Entry<String, SigmaRule> entry : rulesList.entrySet()) {
            predicates[i] = new SigmaRulePredicate();
            predicates[i].setRule(entry.getValue());
            i++;
        }
    }

    @BeforeAll
    void setUp() {
    }

    @After
    public void tearDown() {
        td.close();
    }

    @Test
    public void checkCombinedRules()
        throws IOException, InvalidSigmaRuleException {

        String testRule = "title: Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo: 'ab*'\n"
            + "  condition: test";

        String testRule2 = "title: Aggregate Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: '^.{5}.*$'\n"
            + "  timeframe: 10s\n"
            + "  condition: test | count() > 5"; // count greater than 6

        SigmaRulesFactory srf = new SigmaRulesFactory();;
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http", testRule);
        srf.addRule("Aggregate Http", testRule2);
        initializePredicates(srf);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        topology = stream.createTopology(predicates);
        td = new TopologyTestDriver(topology, getProperties());

        inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        outputTopic = td.createOutputTopic("test-output", Serdes.String().deserializer(),
            Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        // Should be 2 matches
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http") ||
            results.getSigmaMetaData().getTitle().equals("Aggregate Http"));
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http") ||
            results.getSigmaMetaData().getTitle().equals("Aggregate Http"));
        assertTrue(outputTopic.isEmpty());


    }

}
