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

package io.confiuent.sigmaui.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confiuent.sigmaui.models.RegexRule;
import io.confiuent.sigmaui.rules.RegexRulesStore;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class RegexStream {
    final static Logger logger = LogManager.getLogger(RegexStream.class);
    private KafkaStreams streams;

    @Value("${bootstrap.server}")
    private String bootstrapAddress;

    @Value("${schema.registry}")
    private String schemaRegistry;

    @Value("${confluent.regex.inputTopic}")
    private String inputTopic;

    @Value("${confluent.regex.applicationID}")
    private String applicationID;

    @Value("${confluent.regex.filterField}")
    private String filterField;

    @Value("${confluent.regex.regexField}")
    private String regexField;

    @Autowired
    private RegexRulesStore rules;

    private RegexRule currentRule = null;

    @PostConstruct
    private void startStream() {
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, this.applicationID);
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapAddress);
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        createTopic(prop);
        this.streams = new KafkaStreams(createTopology(), prop);

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private void createTopic(Properties prop) {
        AdminClient client = AdminClient.create(prop);
        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(this.inputTopic, 1, (short)1));
        client.createTopics(topics);
        client.close();
    }

    private Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> regexStream = builder.stream(this.inputTopic);

        regexStream.filter((k, v) -> regexFilter(v))
                .mapValues(value -> buildResults(value))
                .filter((k, v) -> {
                    if (v.startsWith("Error")) {
                        logger.info(v);
                        return false;
                    }

                    return true;
                })
                .to((key, value, recordContext) ->  {
                    String outputTopic = "DefaultRegexTopic";
                    if (currentRule != null) {
                        outputTopic = currentRule.getOutputTopic();
                    }

                    logger.info("output topic: " + outputTopic);
                    return outputTopic;
                }, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    public Boolean regexFilter(String value) {
        //logger.info("regexFilter: " + value);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> streamData = null;
        try {
            streamData = objectMapper.readValue(value, new TypeReference<Map<String,Object>>(){});
            if (streamData.get(filterField) != null) {
                Set<String> regexFilterNames = rules.getRuleNames();
                for (String filterName : regexFilterNames) {
                    if (streamData.get(filterField).toString().equals(filterName) &&
                            streamData.get(regexField) != null) {
                        return true;
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean regexFilterTest(String value, String filterName) {
        logger.info("regexFilter: " + value);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> streamData = null;
        try {
            streamData = objectMapper.readValue(value, new TypeReference<Map<String,Object>>(){});
            if (streamData.get(filterField) != null) {
                if (streamData.get(filterField).toString().equals(filterName) &&
                        streamData.get(regexField) != null) {
                    return true;
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String buildResults(String value) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> streamData = null;

        try {
            streamData = mapper.readValue(value, new TypeReference<Map<String,Object>>(){});
            if (streamData.get(filterField) != null) {
                Set<String> regexFilterNames = rules.getRuleNames();
                for (String filterName : regexFilterNames) {
                    if (streamData.get(filterField).toString().equals(filterName) &&
                            streamData.get(regexField) != null) {

                        currentRule = rules.getRule(filterName);
                        String exp = currentRule.getRegex();
                        String[] names = StringUtils.substringsBetween(exp, "<", ">");

                        Pattern p = Pattern.compile(exp);
                        Matcher m = p.matcher(streamData.get(regexField).toString());
                        if (m.find()) {
                            // ...then you can use group() methods.
                            System.out.println("m size: " + m.groupCount());
                            Map<String, String> results = new HashMap<>();
                            for (int i = 0; i < names.length; i++) {
                                System.out.println(names[i] + ": " + m.group(names[i]));
                                results.put(names[i], m.group(names[i]));
                            }

                            results.put("sourceType", currentRule.getSourceType());
                            for(Map.Entry<String,String> custom : currentRule.getCustomFields().entrySet()) {
                                results.put(custom.getKey(), custom.getValue());
                            }

                            return mapper.writeValueAsString(results);
                        }
                    }
                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error: unable to parse source data";
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            //return "Error: bad regular expression";
        }

        return "Error: bad regular expression";
    }

    private String buildResultsTest(String value, String filterName, RegexRule rule) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> streamData = null;

        try {
            streamData = mapper.readValue(value, new TypeReference<Map<String,Object>>(){});
            if (streamData.get(filterField) != null) {

                if (streamData.get(filterField).toString().equals(filterName) &&
                        streamData.get(regexField) != null) {

                    currentRule = rule;
                    String exp = currentRule.getRegex();
                    String[] names = StringUtils.substringsBetween(exp, "<", ">");

                    Pattern p = Pattern.compile(exp);
                    Matcher m = p.matcher(streamData.get(regexField).toString());
                    if (m.find()) {
                        // ...then you can use group() methods.
                        System.out.println("m size: " + m.groupCount());
                        Map<String, String> results = new HashMap<>();
                        for (int i = 0; i < names.length; i++) {
                            System.out.println(names[i] + ": " + m.group(names[i]));
                            results.put(names[i], m.group(names[i]));
                        }

                        return mapper.writeValueAsString(results);
                    }

                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "unable to parse source data";
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return "bad regular expression";
        }

        return "bad regular expression";
    }

    public static void main(String[] args) {
        RegexStream regex = new RegexStream();
        regex.filterField = "sourcetype";
        regex.regexField = "event";

        //String json = "{\"sourcetype\":\"pfsense\",\"source\":\"/var/log/messages\",\"time\":\"1622778211\",\"index\":\"main\",\"event\":\"Jun  3 20:43:31 filterlog[15121]: 8,,,1000000103,em0,match,block,in,4,0x0,,43,6291,0,none,6,tcp,44,162.142.125.83,64.71.189.18,16568,12283,0,S,2868024203,,1024,,mss\",\"host\":\"ryzen.ctolab.fabrix.com\"}";

        String json = "{\"event\":\"Aug 11 16:05:52 boundary-fw-1 %ASA-4-106023: Deny udp src inside:192.168.9.20/38524 dst outside:192.168.10.106/514 by access-group \\\"inside_access_in\\\" [0x0, 0x0]\",\"sourcetype\":\"cisco:asa\"}";
        //String json = "{\"event\":\"Aug 09 22:53:26 boundary-fw-1 %ASA-2-106001: Inbound TCP connection denied from 34.215.24.225/443 to 192.168.10.18/10860 flags FIN ACK  on interface outside\",\"time\":1628548712,\"host\":\"boundary-fw-1\",\"source\":\"udp:514\",\"index\":\"main\",\"sourcetype\":\"cisco:asa\",\"_savedPort\":\"3333\"}";
        RegexRule rule = new RegexRule();
        //rule.setRegex("^(?<month>\\w{3})\\s(?<day>\\d{2})\\s(?<hour>\\d{2})\\:(?<min>\\d{2})\\:(?<sec>\\d{2})\\s");
        rule.setRegex("^(?<timestamp>\\w{3}\\s\\d{2}\\s\\d{2}:\\d{2}:\\d{2})\\s");

        rule.setSourceType("cisco:asa");


        if (regex.regexFilterTest(json, "cisco:asa") == true) {
            System.out.println("Results: " + regex.buildResultsTest(json, "cisco:asa", rule));
        }

    }
}
