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
public class ComplexConditionsTest {
    final static Logger logger = LogManager.getLogger(KafkaRuleTest.class);

    private TopologyTestDriver td;
    private Topology topology;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;
    private ObjectMapper objectMapper = new ObjectMapper();


    Properties getProperties() {
        Properties testProperties = new Properties();
        //TODO should be bootstrap.serverS -- use defined variable in place of string
        //testProperties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        testProperties.setProperty("bootstrap.servers", "foo:1234");
        testProperties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test-simple");
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
        td.close();
    }

    @Test
    public void checkAndCondition()
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {

        String testKafkaRule = "title: Simple Http Kafka\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  foo_text:\n"
            + "   text: 'abc'\n"
            + "  foo_number:\n"
            + "   number: '123'\n"
            + "  condition: foo_text AND foo_number\n"
            + "kafka:\n"
            + "  outputTopic: my-test-results";


        SigmaRulesFactory srf = new SigmaRulesFactory();
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http Kafka", testKafkaRule);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        topology = stream.createTopology();
        td = new TopologyTestDriver(topology, getProperties());

        // first test the default output topic
        inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        outputTopic = td.createOutputTopic("my-test-results",
            Serdes.String().deserializer(), Serdes.String().deserializer());

        assertTrue(outputTopic.isEmpty());

        inputTopic.pipeInput("{\"text\" : \"abc\", \"number\" : \"123\"}");
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http Kafka"));
        assertTrue(outputTopic.isEmpty());

    }

    @Test
    public void checkFilterCondition()
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {

        String testKafkaRule = "title: Simple Http Kafka\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  sourcetype:\n"
            + "   sourcetype: 'cisco:asa'\n"
            + "  condition: sourcetype\n"
            + "kafka:\n"
            + "  outputTopic: my-test-results";

        String dataInput = "{\n"
            + "\"event\": \"Aug 11 16:05:52 boundary-fw-1 %ASA-4-106023: Deny udp src inside:192.168.9.20/38524 dst outside:192.168.10.106/514 by access-group \\\"inside_access_in\\\" [0x0, 0x0]\",\n"
            + "\"time\": 1691769472,\n"
            + "\"host\": \"boundary-fw-1\",\n"
            + "\"source\": \"udp:514\",\n"
            + "\"index\": \"main\",\n"
            + "\"sourcetype\": \"cisco:asa\",\n"
            + "\"_savedPort\": \"3333\"\n"
            + "}";

        SigmaRulesFactory srf = new SigmaRulesFactory();
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http Kafka", testKafkaRule);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        topology = stream.createTopology();
        td = new TopologyTestDriver(topology, getProperties());

        // first test the default output topic
        inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        outputTopic = td.createOutputTopic("my-test-results",
            Serdes.String().deserializer(), Serdes.String().deserializer());

        // verify nothing is in the topic yet
        assertTrue(outputTopic.isEmpty());

        // add some valid data
        inputTopic.pipeInput(dataInput);
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http Kafka"));
        assertTrue(outputTopic.isEmpty());

        // add some invalid data
        inputTopic.pipeInput("{\"text\" : \"abc\", \"number\" : \"123\"}");
        assertTrue(outputTopic.isEmpty());

    }

    @Test
    public void checkComplexRegexCondition()
        throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {

        String testKafkaRule = "title: Simple Http Kafka\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  patternMatch:\n"
            + "    event|re: \'^(?<timestamp>\\w{3}\\s\\d{2}\\s\\d{2}:\\d{2}:\\d{2})\\s(?<hostname>[^\\s]+)\\s\\%ASA-\\d-(?<messageID>[^:]+):\\s(?<action>[^\\s]+)\\s(?<protocol>[^\\s]+)\\ssrc\\sinside:(?<src>[0-9\\.]+)\\/(?<srcport>[0-9]+)\\sdst\\soutside:(?<dest>[0-9\\.]+)\\/(?<destport>[0-9]+)\'\n"
            + "  condition: patternMatch\n"
            + "kafka:\n"
            + "  inputTopic: my-input-topic\n"
            + "  outputTopic: my-test-results\n"
            + "  customFields:\n"
            + "    location: edge\n"
            + "    sourcetype: cisco:asa\n"
            + "    index: main";

        String dataInput = "{\n"
            + "\"event\": \"Aug 11 16:05:52 boundary-fw-1 %ASA-4-106023: Deny udp src inside:192.168.9.20/38524 dst outside:192.168.10.106/514 by access-group \\\"inside_access_in\\\" [0x0, 0x0]\",\n"
            + "\"time\": 1691769472,\n"
            + "\"host\": \"boundary-fw-1\",\n"
            + "\"source\": \"udp:514\",\n"
            + "\"index\": \"main\",\n"
            + "\"sourcetype\": \"cisco:asa\",\n"
            + "\"_savedPort\": \"3333\"\n"
            + "}";

        SigmaRulesFactory srf = new SigmaRulesFactory();
        srf.setFiltersFromProperties(getProperties());
        srf.addRule("Simple Http Kafka", testKafkaRule);

        SigmaStream stream = new SigmaStream(getProperties(), srf);
        topology = stream.createTopology();
        td = new TopologyTestDriver(topology, getProperties());

        // first test the default output topic
        inputTopic = td.createInputTopic("test-input", Serdes.String().serializer(),
            Serdes.String().serializer());
        outputTopic = td.createOutputTopic("my-test-results",
            Serdes.String().deserializer(), Serdes.String().deserializer());

        // verify nothing is in the topic yet
        assertTrue(outputTopic.isEmpty());

        // add some valid data
        inputTopic.pipeInput(dataInput);
        DetectionResults results = objectMapper.readValue(outputTopic.readValue(), DetectionResults.class);
        assertTrue(results.getSigmaMetaData().getTitle().equals("Simple Http Kafka"));
        assertTrue(results.getSigmaMetaData().getCustomFields().containsKey("timestamp"));
        System.out.println(results.toPrettyJSON());
        assertTrue(outputTopic.isEmpty());

        // add some invalid data
        inputTopic.pipeInput("{\"text\" : \"abc\", \"number\" : \"123\"}");
        assertTrue(outputTopic.isEmpty());

    }

}
