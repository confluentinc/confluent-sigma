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

package io.confluent.sigmarules.tools;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

public class AggregateTopologyViewer {

  public static void main(String[] args)
      throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {
    Properties testProperties = new Properties();
    testProperties.setProperty("bootstrap.server", "localhost:9092");
    testProperties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test-simple");
    testProperties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testProperties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testProperties.setProperty("sigma.rules.topic", "rules");
    testProperties.setProperty("schema.registry", "localhost:8888");
    testProperties.setProperty("output.topic", "test-output");
    testProperties.setProperty("data.topic", "test-input");


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
    srf.setFiltersFromProperties(testProperties);
    srf.addRule("Simple Http", testRule);
    srf.addRule("Simple Http2", testRule);
    srf.addRule("Simple Http3", testRule);
    srf.addRule("Simple Http4", testRule);
    srf.addRule("Simple Http5", testRule);

    SigmaStream stream = new SigmaStream(testProperties, srf);
    Topology topology = stream.createTopology();
    System.out.println(topology.describe());

  }
}
