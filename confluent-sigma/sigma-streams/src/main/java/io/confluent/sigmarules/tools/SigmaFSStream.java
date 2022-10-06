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

package io.confluent.sigmarules.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRulesStore;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

public class SigmaFSStream {
    private Properties config; 
    private KafkaStreams streams;
    private SigmaRulesStore sigmaRulesManager;

    public SigmaFSStream(SigmaRulesStore sigmaRulesManager) {
        this.config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "sigma-rules-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // create the rules cache
        this.sigmaRulesManager = sigmaRulesManager;

    }

    private StreamsBuilder createBuilder() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sigmaRulesRaw = builder.stream("sigma_rules_fs");
        KStream<String, String> sigmaRule = sigmaRulesRaw.map((k, v) -> createSigmaRule(v));
        sigmaRule.to("sigma_rules", Produced.with(Serdes.String(), Serdes.String()));

        return builder;
    }

    public void startStream() {
        this.streams = new KafkaStreams(createBuilder().build(), config);
        streams.cleanUp();
        streams.start();
 

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private KeyValue<String, String> createSigmaRule(String rule) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String key = null;
        String value = parseFSConnectorYaml(rule);
        SigmaRule sigmaRule;

        try {
            sigmaRule = mapper.readValue(value, SigmaRule.class);
            System.out.println("title: " + sigmaRule.getTitle());

            key = sigmaRule.getTitle();
            // add the rule to the cache
            //sigmaRulesManager.addRule(sigmaRule.getTitle(), parsedYaml);

        } catch (JsonProcessingException e) {
            System.out.println(value);
            e.printStackTrace();
        }

        return new KeyValue<>(key, value);
    }

    private String parseFSConnectorYaml(String fsConnectYaml) {
        // find the payload
        String ruleYaml = StringUtils.substringAfter(fsConnectYaml, "\"payload\":");
        // remove closing bracket
        ruleYaml = StringUtils.removeEnd(ruleYaml, "}");
        return ruleYaml;
    }
}
