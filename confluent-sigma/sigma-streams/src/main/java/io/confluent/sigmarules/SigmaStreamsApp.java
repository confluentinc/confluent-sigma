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

import io.confluent.sigmarules.config.SigmaOptions;
import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.streams.StreamManager;
import java.io.File;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SigmaStreamsApp {
    final static Logger logger = LogManager.getLogger(SigmaStreamsApp.class);

    private StreamManager streamManager;
    private SigmaRulesFactory ruleFactory;
    private SigmaStream sigmaStream;

    // this will initialize using environment variable (i.e. from Docker)
    private void initializeWithEnv() {
        Properties properties = getPropertiesFromEnv();
        initializeWithProps(properties);
    }

    // this will initialize using arguments passed in (i.e. -c arg)
    private void initializeWithProps(Properties properties) {
        this.streamManager = new StreamManager(properties);
        this.ruleFactory = new SigmaRulesFactory(streamManager.getStreamProperties());
        this.sigmaStream = new SigmaStream(streamManager.getStreamProperties(), ruleFactory);

        sigmaStream.startStream();
    }

    public boolean isDockerized() {
        File f = new File("/.dockerenv");
        return f.exists();
    }

    private Properties getPropertiesFromEnv() {
        Properties props = new Properties();
        System.getenv().forEach((k, v) -> {
            String newKey = k.replace("_", ".");
            System.out.println(newKey + ": " + v);
            props.setProperty(newKey, v);
        });

        return props;
    }

    public static void main(String[] args) {
        logger.info("Starting SigmaStreamsApp");
        if (logger.getLevel().isLessSpecificThan(Level.INFO))
        {
            String message = "Passed in arguments: ";
            for (int i = 0; i < args.length; i++)
                message = message + args[i] + " ";
            logger.log(Level.INFO, message);
        }

        SigmaStreamsApp sigma = new SigmaStreamsApp();
        if (sigma.isDockerized()) {
            logger.info("Initialize SigmaStreamsApp from environment variables");
            sigma.initializeWithEnv();
        } else {
            logger.info("Initialize SigmaStreamsApp from properties file");
            SigmaOptions sigmaOptions = new SigmaOptions(args);
            sigma.initializeWithProps(sigmaOptions.getProperties());
        }
    }
}

