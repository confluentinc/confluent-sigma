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

import io.confluent.sigmarules.streams.SigmaStream;
import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class SigmaAppInstanceStore implements KafkaStreams.StateListener  {
    final static Logger logger = LogManager.getLogger(SigmaAppInstanceStore.class);

    public static final String KEY_CONVERTER_SCHEMA_REGISTRY_URL = "key.converter.schema.registry.url";
    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";
    private static final long STATE_POLL_SLEEP = 30000;

    private volatile Poller poller;
    private SigmaStream sigmaStreamApp;
    private SigmaAppInstanceState state;
    private Properties props;

    private Cache<String, SigmaAppInstanceState> sigmaAppInstanceStateCache;

    public SigmaAppInstanceStore(Properties properties, SigmaStream sigmaStreamApp) {
        this.sigmaStreamApp = sigmaStreamApp;
        this.props = properties;
        state = new SigmaAppInstanceState();
        initialize(properties);
    }

    public void initialize(Properties properties) {
        Properties kcacheProps = new Properties(properties);
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG,
                properties.getProperty(SigmaPropertyEnum.BOOTSTRAP_SERVER.toString()));

        String sigmaAppTopic = properties.getProperty(SigmaPropertyEnum.SIGMA_APP_TOPIC.toString());
        if (sigmaAppTopic == null) sigmaAppTopic = SigmaPropertyEnum.SIGMA_APP_TOPIC.getDefaultValue();
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, sigmaAppTopic);

        // optional config parameters
        if (properties.containsKey(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SECURITY_PROTOCOL_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()));

        if (properties.containsKey(SigmaPropertyEnum.SASL_MECHANISM.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SASL_MECHANISM_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SASL_MECHANISM.toString()));

        if (properties.containsKey("sasl.jaas.config"))
            kcacheProps.setProperty("kafkacache.sasl.jaas.config",
                properties.getProperty("sasl.jaas.config"));

        if (properties.containsKey("sasl.client.callback.handler.class"))
            kcacheProps.setProperty("kafkacache.sasl.client.callback.handler.class",
                    properties.getProperty("sasl.client.callback.handler.class"));

        if (properties.containsKey(SigmaPropertyEnum.SCHEMA_REGISTRY.toString())) {
            kcacheProps.setProperty(KEY_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
            kcacheProps.setProperty(VALUE_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
        }

        sigmaAppInstanceStateCache = new KafkaCache<String, SigmaAppInstanceState>(
            new KafkaCacheConfig(kcacheProps),
            Serdes.String(),
            SigmaAppInstanceState.getJsonSerde());

        sigmaAppInstanceStateCache.init();

    }

    private void createThread()
    {
        poller = new Poller();
    }

    public void update()
    {
        KafkaStreams kStreams = sigmaStreamApp.getStreams();

        state.setApplicationId(sigmaStreamApp.getApplicationId());
        KafkaStreams.State streamsState = kStreams.state();
        state.setKafkaStreamsState(streamsState.toString());

        if (streamsState.isRunningOrRebalancing()) {
            Collection<StreamsMetadata> metadataCollection =
                    kStreams.metadataForAllStreamsClients();
            List<String> hostList = new ArrayList<String>();
            for (StreamsMetadata metadata : metadataCollection) {
                hostList.add(metadata.hostInfo().toString());
            }
            state.setHosts(hostList);
        } else {
            state.setHosts(null);
        }

        state.setNumRules(sigmaStreamApp.getRuleFactory().getSigmaRules().size());
        push();
    }

    public void register()
    {
        sigmaStreamApp.getStreams().setStateListener(this);
        createThread();
    }

    private void push()
    {
        sigmaAppInstanceStateCache.put(state.getKey(),state);
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        update();
    }

    private class Poller {
        private Thread pollThread;
        private boolean running = true;

        public Poller() {
            pollThread = new Thread(new Runnable() {
                public void run() {
                    while (running) {
                        try {
                            Thread.sleep(STATE_POLL_SLEEP); // sleep for 1 second
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        update();
                    }
                }
            });
            pollThread.start();
        }

        public void stop() {
            running = false;
        }
    }
}
