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

package io.confluent.sigmarules.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.models.SigmaDetection;
import io.confluent.sigmarules.models.SigmaDetections;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import io.confluent.sigmarules.tools.SigmaRuleLoader;
import io.confluent.sigmarules.utilities.SigmaOptions;
import java.io.IOException;
import java.util.Properties;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigmaRulesFactoryTestAdd {
    @Test
    void testRuleAdded() throws InvalidSigmaRuleException, IOException {
        Properties testProperties = new Properties();
        testProperties.setProperty("sigma.rule.filter.product", "zeek");

        SigmaRulesFactory srf = new SigmaRulesFactory();
        srf.setFiltersFromProperties(testProperties);

        String testRule = "title: Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo: 'ab*'\n"
            + "  condition: test";

        srf.addRule("Simple Http", testRule);
        Assertions.assertFalse(srf.isRuleFiltered("Simple Http"));
    }
}
