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

import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaRulePredicate;
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.streams.StreamManager;
import io.confluent.sigmarules.utilities.SigmaOptions;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaStreamsApp extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStreamsApp.class);

    private SigmaRulesFactory ruleFactory;
    private SigmaRulePredicate[] predicates;
    private SigmaStream sigmaStream;

    public SigmaStreamsApp(SigmaOptions options) {
        super(options.getProperties());

        createSigmaRules();
        createSigmaStream();

        this.ruleFactory.addObserver((rule -> handleNewRule(rule)), false);
    }

    private void handleNewRule(SigmaRule newRule) {
        logger.info("Received a new rule: " + newRule.getTitle());

        // need to stop the stream and rebuild the topology
        //                if (streams != null) {
        //                    streams.close();
        //                    startStream();
        //                }
    }

    private void createSigmaRules() {
        this.initializePredicates();
        this.ruleFactory = new SigmaRulesFactory(properties);
    }

    private void createSigmaStream() {
        // initialize and start the main sigma stream
        this.sigmaStream = new SigmaStream(properties, ruleFactory);
    }

    private void initializePredicates() {
        // initialize the predicates
        Map<String, SigmaRule> rulesList = ruleFactory.getSigmaRules();
        logger.info("number of rules " + rulesList.size());

        Integer i = 0;
        predicates = new SigmaRulePredicate[rulesList.size()];
        for (Map.Entry<String, SigmaRule> entry : rulesList.entrySet()) {
            predicates[i] = new SigmaRulePredicate();
            predicates[i].setRule(entry.getValue());
            i++;
        }
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
