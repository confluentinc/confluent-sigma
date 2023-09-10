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

package io.confluent.sigmarules.appState;

import io.confluent.sigmarules.config.KcacheConfig;
import io.confluent.sigmarules.config.SigmaPropertyEnum;
import io.confluent.sigmarules.parsers.ParsedSigmaRule;
import io.confluent.sigmarules.streams.SigmaStream;
import io.kcache.*;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SigmaAppInstanceStore implements KafkaStreams.StateListener  {
    final static Logger logger = LogManager.getLogger(SigmaAppInstanceStore.class);

    private long statePollSleep;
    private SigmaStream sigmaStreamApp;
    private Cache<String, SigmaAppInstanceState> sigmaAppInstanceStateCache;

    public SigmaAppInstanceStore(Properties properties) {
        this.sigmaStreamApp = null;
        initialize(properties);
    }

    public SigmaAppInstanceStore(Properties properties, SigmaStream sigmaStreamApp) {
        this.sigmaStreamApp = sigmaStreamApp;
        initialize(properties);

        // Perform cleanup operations here, such as closing database connections, file handles, etc.
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanState));
    }

    public void initialize(Properties properties) {
        Properties kcacheProps = KcacheConfig.createConfig(properties, SigmaPropertyEnum.SIGMA_APP_TOPIC);

        // this property should only be set for testing or if you really want to skip the app registration
        if (properties.containsKey("skip.app.registration") == false) {
            sigmaAppInstanceStateCache = new KafkaCache<>(
                new KafkaCacheConfig(kcacheProps),
                Serdes.String(),
                SigmaAppInstanceState.getJsonSerde());

            sigmaAppInstanceStateCache.init();

            // Initialize with default so that even if an exception occurs parsing passed in parameter
            // we have the right value
            if (sigmaStreamApp != null) {
                statePollSleep = Long.parseLong(SigmaPropertyEnum.SIGMA_APP_STATE_POLL_SLEEP.getDefaultValue());
                if (properties.contains(SigmaPropertyEnum.SIGMA_APP_STATE_POLL_SLEEP)) {
                    try {
                        statePollSleep = Long.
                                parseLong(properties.getProperty(SigmaPropertyEnum.SIGMA_APP_STATE_POLL_SLEEP.toString()));
                    } catch (Exception e) {
                        logger.warn("Unable to parse value for " + SigmaPropertyEnum.SIGMA_APP_STATE_POLL_SLEEP +
                                ". Using default " + SigmaPropertyEnum.SIGMA_APP_STATE_POLL_SLEEP , e);
                    }
                }

                cleanZombieState();
            }
        }
    }

    private void cleanZombieState() {
        logger.debug("cleaning up zombie state");
        String applicationId = sigmaStreamApp.getApplicationId();
        KeyValueIterator<String, SigmaAppInstanceState> iterator = sigmaAppInstanceStateCache.all();
        while (iterator.hasNext()) {
            KeyValue<String, SigmaAppInstanceState> next = iterator.next();
            SigmaAppInstanceState state = next.value;
            logger.warn("Found zombie state with key " + next.key);
            if (state.getApplicationId() == applicationId) sigmaAppInstanceStateCache.remove(next.key);
        }
    }

    private void createThread()
    {
        new Poller();
    }

    private SigmaAppInstanceState createSigmaAppInstanceState() {
        SigmaAppInstanceState state = new SigmaAppInstanceState();
        state.popuplate(sigmaStreamApp);
        return state;
    }

    public void update()
    {
        logger.debug("Updating Sigma App Instance registration");
        SigmaAppInstanceState state = createSigmaAppInstanceState();
        push(state);
    }

    private void cleanState() {
        logger.info("Cleaning up sigma app instance state");
        SigmaAppInstanceState state = createSigmaAppInstanceState();
        sigmaAppInstanceStateCache.remove(state.getKey());
        sigmaAppInstanceStateCache.flush();
    }

    public void register()
    {
        sigmaStreamApp.getStreams().setStateListener(this);
        createThread();
    }

    private void push(SigmaAppInstanceState state)
    {
        sigmaAppInstanceStateCache.put(state.getKey(),state);
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        update();
    }

    public Set<String> getInstanceIDs() { 
        return sigmaAppInstanceStateCache.keySet();
    }

    public List<SigmaAppInstanceState> getStates() {
        return new ArrayList<SigmaAppInstanceState>(sigmaAppInstanceStateCache.values());
    }

    private class Poller {
        private Thread pollThread;
        private boolean running = true;

        @SuppressWarnings("BusyWait")
        public Poller() {
            pollThread = new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(statePollSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    update();
                }
            });
            pollThread.start();
        }

        public void stop() {
            running = false;
        }
    }
}
