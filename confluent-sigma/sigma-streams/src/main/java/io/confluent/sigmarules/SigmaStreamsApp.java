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

package io.confluent.sigmarules;

import io.confluent.sigmarules.models.SigmaRulePredicate;
import io.confluent.sigmarules.rules.SigmaRuleManager;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.AggregateStreamsFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.streams.StreamManager;
import io.confluent.sigmarules.utilities.SigmaOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SigmaStreamsApp extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStreamsApp.class);

    private SigmaRulesFactory ruleFactory;
    private final Integer DEFAULT_NUMBER_SIGMA_RULES = 10;
    private SigmaRulePredicate[] predicates;
    private SigmaStream sigmaStream;
    private AggregateStreamsFactory aggregateStreams;

    public SigmaStreamsApp(SigmaOptions options) {
        super(options.getProperties());

        createSigmaRules();
        createAggregateStreams();
        createSigmaStream();

        this.ruleFactory.addObserver((rule -> handleNewRule(rule)) , true);
    }

    private void createSigmaRules() {
        this.initializePredicates();
        this.ruleFactory = new SigmaRulesFactory(this.properties);
    }

    private void createAggregateStreams() {
        aggregateStreams = new AggregateStreamsFactory(this.properties);

        for (Map.Entry<String, SigmaRuleManager> sigmaRule : ruleFactory.getSigmaRules().entrySet()) {
            String title = sigmaRule.getKey();
            SigmaRuleManager ruleManager = sigmaRule.getValue();
            if (ruleManager.getConditions().hasAggregateConditon()) {
                logger.info("Creating aggregate stream");
                aggregateStreams.createAggregateStream(title, ruleManager);
            }
        }
    }

    private void createSigmaStream() {
        // initialize and start the main sigma stream
        this.sigmaStream = new SigmaStream(this.properties, this.ruleFactory);
    }

    private void initializePredicates() {
        // initialize the predicates
        Integer numRules = Integer.getInteger(properties.getProperty("sigma.max.rules"));
        if (numRules == null) {
            numRules = DEFAULT_NUMBER_SIGMA_RULES;
        }

        predicates = new SigmaRulePredicate[numRules];
        for (int i = 0; i < predicates.length; i++) {
            predicates[i] = new SigmaRulePredicate();
        }
    }

    private void handleNewRule(SigmaRuleManager newRule) {
        // not the most elegant solution but stop the stream to add a new rule filter
        // need to recreate the stream with the added filter
        // probably a much better way to do this
        // second option is to have a preset list of rules and add to that dynamically
        logger.info("Adding a new stream filter for: " + newRule.getRuleTitle());

        for (int i = 0; i < predicates.length; i++) {
            SigmaRulePredicate predicate = predicates[i];
            if (predicate.getRule() == null) {
                predicate.setRule(newRule);

                if (newRule.getConditions().hasAggregateConditon()) {
                    logger.info("Creating aggregate stream");
                    aggregateStreams.createAggregateStream(newRule.getRuleTitle(), newRule);
                }

                break;
            }
        }

        // if we run out of filter predicates, we need to increase the array size
        // and rebuild the topology
        //                if (streams != null && (currentSigmaRuleCount % INITIAL_NUMBER_SIGMA_RULES == 0)) {
        //                    streams.close();
        //                    startStream();
        //                }
    }

    protected void start()
    {
        sigmaStream.startStream(predicates);
    }

    public static void main(String[] args) {
        SigmaStreamsApp sigma = new SigmaStreamsApp(new SigmaOptions(args));
        sigma.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
