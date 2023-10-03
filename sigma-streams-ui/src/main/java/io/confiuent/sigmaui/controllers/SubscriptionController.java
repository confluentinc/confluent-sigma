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

package io.confiuent.sigmaui.controllers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.confiuent.sigmaui.config.SigmaUIProperties;
import nonapi.io.github.classgraph.utils.StringUtils;

@Controller
@RestController
public class SubscriptionController {
    private Map<String, List<JsonNode>> subscriptionData = Collections.synchronizedMap(new HashMap<>());
    private KafkaConsumer<String, String> consumer;
    private Collection<String> topicList = new ArrayList<>();

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    SigmaUIProperties properties;

    @PostConstruct
    private void startTask() {
        Properties props = properties.getProperties();
        consumer = new KafkaConsumer<>(props);
        topicList = Arrays.asList(props.getProperty("topic.list").split("\\s*,\\s*"));
        consumer.subscribe(topicList);
 
        // create the containers for each subscription
        for (String topic : topicList) {
            subscriptionData.put(topic, new ArrayList<>());
        }

        Thread thread = new Thread() {
            public void run() {
                ObjectMapper mapper = new ObjectMapper();
                long currentTime = System.currentTimeMillis();
                long lastSend = currentTime;

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(20));
                    //System.out.println("num records: " + records.count());
                    for (ConsumerRecord<String, String> record : records) {

                        if (record.value() != null) {
                            try {
                                
                                List<JsonNode> dataList = subscriptionData.get(record.topic());
                                dataList.add(mapper.readTree(record.value()));
                            
                                // if the list is larger, send it right away
                                //System.out.println("data list size: " + dataList.size());
                                if (dataList.size() > 500) {
                                    sendBufferedData();
                                }
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.printf("the value is null");
                        }
                    }

                    currentTime = System.currentTimeMillis();
                    if ((currentTime - lastSend) >= 1000) {
                        lastSend = currentTime;
                        sendBufferedData();
                    }
                }
            }
        };
        thread.start();
    }

    @GetMapping({"topicData/{topic}"})
    public List<JsonNode> getTopicData(@PathVariable String topic) {
        List<JsonNode> dataList = new ArrayList<>();
        synchronized (subscriptionData) {
            dataList.addAll(subscriptionData.get(topic));
            subscriptionData.get(topic).clear();
        }

        System.out.println("getTopicData topic : " + topic + " size: " + dataList.size());
        return dataList;

    }

    private void sendBufferedData() {
        //System.out.println("sending buffered data");
        subscriptionData.forEach((topic, list) -> {
            if (list.size() > 0)
               this.template.convertAndSend("/topic/" + topic, list);
               list.clear();
        });
    }
}
