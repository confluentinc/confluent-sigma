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
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.streams.StreamManager;
import io.confluent.sigmarules.utilities.SigmaOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaStreamsApp extends StreamManager {
    final static Logger logger = LogManager.getLogger(SigmaStreamsApp.class);

    private SigmaRulesFactory ruleFactory;
    private SigmaStream sigmaStream;

    public SigmaStreamsApp(SigmaOptions options) {
        super(options.getProperties());

        createSigmaRules();
        createSigmaStream();

        this.ruleFactory.addObserver((this::handleNewRule), false);
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
        this.ruleFactory = new SigmaRulesFactory(properties);
    }

    private void createSigmaStream() {
        // initialize and start the main sigma stream
        this.sigmaStream = new SigmaStream(properties, ruleFactory);
    }

    protected void start()
    {
        sigmaStream.startStream();
    }

    public static void main(String[] args) {
        if (logger.getLevel().isLessSpecificThan(Level.INFO))
        {
            String message = "Starting SigmaSteamsApp with arguments: ";
            for (int i = 0; i < args.length; i++)
                message = message + args[i] + " ";
            logger.log(Level.INFO, message);
        }

        SigmaStreamsApp sigma = new SigmaStreamsApp(new SigmaOptions(args));
        sigma.start();
    }
}
