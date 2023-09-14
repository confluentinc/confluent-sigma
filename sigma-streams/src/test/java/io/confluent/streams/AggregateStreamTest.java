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
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import java.io.IOException;
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
public class AggregateStreamTest {
    final static Logger logger = LogManager.getLogger(AggregateStreamTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();


    Properties getProperties() {
        Properties testProperties = new Properties();
        //TODO should be bootstrap.serverS -- use defined variable in place of string
        //testProperties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        testProperties.setProperty("bootstrap.servers", "foo:1234");
        testProperties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test-multi");
        testProperties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        testProperties.setProperty("sigma.rules.topic", "rules");
        testProperties.setProperty("schema.registry", "localhost:8888");
        testProperties.setProperty("output.topic", "test-output");
        testProperties.setProperty("data.topic", "test-input");
        testProperties.setProperty("skip.app.registration", "true");

        return testProperties;
    }

    @BeforeAll
    void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void checkSingleRule()
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {

        String testRule = "title: Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: '^.{5}.*$'\n"
            + "  timeframe: 10s\n"
            + "  condition: test | count() > 5";

        SigmaRulesFactory srf = new SigmaRulesFactory();;
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http", testRule);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        Topology topology = stream.createTopology();
        TopologyTestDriver td = new TopologyTestDriver(topology, getProperties());

        TestInputTopic<String, String> inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        TestOutputTopic<String, String> outputTopic = td.createOutputTopic("test-output", Serdes.String().deserializer(),
            Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));

        assertTrue(outputTopic.isEmpty());

        td.close();
    }

    @Test
    public void checkMultipleRules()
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {

        String testRule = "title: Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: '^.{5}.*$'\n"
            + "  timeframe: 10s\n"
            + "  condition: test | count() > 5";

        String testRule2 = "title: Another Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: '^.{5}.*$'\n"
            + "  timeframe: 10s\n"
            + "  condition: test | count() > 6"; // count greater than 6

        SigmaRulesFactory srf = new SigmaRulesFactory();;
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http", testRule);
        srf.addRule("Another Simple Http", testRule2);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        Topology topology = stream.createTopology();
        TopologyTestDriver td = new TopologyTestDriver(topology, getProperties());

        TestInputTopic<String, String> inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        TestOutputTopic<String, String> outputTopic = td.createOutputTopic("test-output", Serdes.String().deserializer(),
            Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");

        // Should be 1 match (titles)
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"foo\" : \"abcde\"}");
        // Should be 2 matches
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Another Simple Http") ||
            results.getSigmaMetaData().getTitle().equals("Simple Http"));
        results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Another Simple Http") ||
            results.getSigmaMetaData().getTitle().equals("Simple Http"));
        assertTrue(outputTopic.isEmpty());

        // Should be empty
        String sampleData2 = "{\"foo\" : \"bc\"}";
        inputTopic.pipeInput(sampleData2);
        assertTrue(outputTopic.isEmpty());

        td.close();

    }

}
