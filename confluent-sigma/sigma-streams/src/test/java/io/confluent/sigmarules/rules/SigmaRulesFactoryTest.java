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

import io.confluent.sigmarules.tools.SigmaRuleLoader;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigmaRulesFactoryTest  {
    private Properties testProperties = new Properties();

    @ClassRule
    //public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1"));
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    @BeforeAll
    void setUp() {
        kafka.start();

        testProperties.setProperty("bootstrap.server", kafka.getBootstrapServers());
        testProperties.setProperty("sigma.rules.topic", "rules");
        testProperties.setProperty("schema.registry", "localhost:8888");

        //Currently the SigmaRuleFactor usesde the ApplicationSettings singleton to pull bootsreap servers and the topic
        //but does not pull the filter product and service.  It should probabyl get all or none of its configuration
        //from there.  For the time being leaving it for a future task

        //testProperties.setProperty("sigma.rule.filter.product", "zeek");
        //testProperties.setProperty("sigma.rule.filter.service", "http");

        //sigma.rule.filter.title=Simple Http
        //ApplicationSettings.getInstance().setConfig(testProperties);

        loadTestRules();
    }

    /**
     * Load the test rules for this unit test
     */
    private void loadTestRules() {
        SigmaRuleLoader sigma = new SigmaRuleLoader(kafka.getBootstrapServers(), "rules");
        sigma.loadSigmaDirectory("config/rules");
    }

    @AfterAll
    public void tearDown() {
        kafka.stop();
    }

    //@Test
    public void testIsFilteredRuleByProductAndServiceOnly() {
        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        srf.setProductAndService("zeek", "http");
        assertFalse("Should be false", srf.isFilteredRule("Simple Http"));
    }

    //@Test
    public void testIsFilteredRuleByProductAndServiceAndNotFilterList() {
        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        srf.addTitleToFilterList("foobar");
        srf.addTitleToFilterList("anotherFoobar");
        srf.setProductAndService("zeek", "http");
        assertTrue("Should be true because it does not match product and service",
                srf.isFilteredRule("Simple Http"));
    }

    //@Test
    public void testIsFilteredRuleByProductAndServiceAndFilterList() {
        SigmaRulesFactory srf = new SigmaRulesFactory(testProperties);
        srf.addTitleToFilterList("Simple Http");
        srf.addTitleToFilterList("anotherFoobar");
        srf.setProductAndService("zeek", "http");
        assertFalse("Should be false because is in the filter list",
                srf.isFilteredRule("Simple Http"));
    }
}
