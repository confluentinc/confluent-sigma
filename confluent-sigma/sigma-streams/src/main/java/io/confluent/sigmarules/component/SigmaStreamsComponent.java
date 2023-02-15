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

package io.confluent.sigmarules.component;

import io.confluent.sigmarules.rules.SigmaRulesFactory;
import io.confluent.sigmarules.streams.SigmaStream;
import io.confluent.sigmarules.streams.StreamManager;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import io.confluent.sigmarules.config.SigmaOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Component
public class SigmaStreamsComponent {
    final static Logger logger = LogManager.getLogger(SigmaStreamsComponent.class);

    @Autowired
    Environment springEnv;

    private StreamManager streamManager;
    private SigmaRulesFactory ruleFactory;
    private SigmaStream sigmaStream;

    @PostConstruct
    private void initialize() {
        Properties properties = getPropertiesFromEnv();
        initializeWithProps(properties);
    }

    private void initializeWithProps(Properties properties) {
        this.streamManager = new StreamManager(properties);
        this.ruleFactory = new SigmaRulesFactory(streamManager.getStreamProperties());
        this.sigmaStream = new SigmaStream(streamManager.getStreamProperties(), ruleFactory);

        sigmaStream.startStream();
    }

    private Properties getPropertiesFromEnv() {
        Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) springEnv).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
            .filter(ps -> ps instanceof EnumerablePropertySource)
            .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
            .flatMap(Arrays::<String>stream)
            .forEach(propName -> props.setProperty(propName, springEnv.getProperty(propName)));

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

        SigmaOptions sigmaOptions = new SigmaOptions(args);
        SigmaStreamsComponent sigma = new SigmaStreamsComponent();
        sigma.initializeWithProps(sigmaOptions.getProperties());
    }
}

