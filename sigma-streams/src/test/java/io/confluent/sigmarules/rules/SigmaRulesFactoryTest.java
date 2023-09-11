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

import io.confluent.sigmarules.tools.SigmaRuleLoader;
import io.confluent.sigmarules.config.SigmaOptions;
import java.util.Properties;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigmaRulesFactoryTest  {

    @ClassRule
    //public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1"));
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    @BeforeAll
    void setUp() {
        kafka.start();
        loadTestRules();
    }

    Properties getProperties() {
        Properties testProperties = new Properties();
        testProperties.setProperty("application.id", "test-app");
        testProperties.setProperty("bootstrap.servers", kafka.getBootstrapServers());
        testProperties.setProperty("sigma.rules.topic", "rules");
        testProperties.setProperty("schema.registry", "localhost:8888");
        testProperties.setProperty("sigma.rules.topic", "test-topic");

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
    }

    @Test
    public void testIsFilteredRuleByProductOnly() {
        Properties testProperties = getProperties();
        testProperties.setProperty("sigma.rule.filter.product", "zeek");

        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        assertFalse("Should be false", srf.isRuleFiltered("Simple Http"));
    }

    @Test
    public void testIsFilteredRuleByServiceOnly() {
        Properties testProperties = getProperties();
        testProperties.setProperty("sigma.rule.filter.service", "http");

        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        assertFalse("Should be false", srf.isRuleFiltered("Simple Http"));
    }

    @Test
    public void testIsFilteredRuleByProductAndServiceOnly() {
        Properties testProperties = getProperties();
        testProperties.setProperty("sigma.rule.filter.product", "zeek");
        testProperties.setProperty("sigma.rule.filter.service", "http");

        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        assertFalse("Should be false", srf.isRuleFiltered("Simple Http"));
    }

    @Test
    public void testIsFilteredRuleByProductAndServiceAndNotFilterList() {
        Properties testProperties = getProperties();
        testProperties.setProperty("sigma.rule.filter.product", "zeek");
        testProperties.setProperty("sigma.rule.filter.service", "http");
        testProperties.setProperty("sigma.rule.filter.title", "foobar");

        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        assertTrue("Should be true because it does not match product and service",
                srf.isRuleFiltered("Simple Http"));
    }

    @Test
    public void testIsFilteredRuleByProductAndServiceAndFilterList() {
        Properties testProperties = getProperties();
        //testProperties.setProperty("sigma.rule.filter.product", "zeek");
        //testProperties.setProperty("sigma.rule.filter.service", "http");
        testProperties.setProperty("sigma.rule.filter.title", "Simple Http");

        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        assertFalse("Should be false because is in the filter list",
                srf.isRuleFiltered("Simple Http"));
    }
}
