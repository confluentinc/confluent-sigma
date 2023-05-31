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

import io.confiuent.sigmaui.models.DNSStreamData;
import io.confluent.sigmarules.models.DetectionResults;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DNSDataController {
    private List<DNSStreamData> dnsData = Collections.synchronizedList(new ArrayList<>());

    private List<DetectionResults> dnsDetections = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private SimpMessagingTemplate template;

    @PostConstruct
    private void startBufferTask() {
        System.out.println("starting timer task");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
              sendBufferedData();
            }}, 0L, 2000L);
    }

    @KafkaListener(topics = {"dns"}, containerFactory = "dnsKafkaListenerContainerFactory")
    public void consumeDNSData(DNSStreamData dns) {
        synchronized (this.dnsData) {
            this.dnsData.add(dns);

            if (this.dnsData.size() > 500) {
                sendBufferedData();
            }
        }
    }

    @KafkaListener(topics = {"dns-detection"}, containerFactory = "dnsDetectionKafkaListenerContainerFactory")
    public void consumeDetectionData(DetectionResults results) {
        synchronized (this.dnsDetections) {
            this.dnsDetections.add(results);

            if (this.dnsDetections.size() > 500) {
                sendBufferedData();
            }
        }
    }

    private void sendBufferedData() {
        synchronized (this.dnsData) {
            if (this.dnsData.size() > 0) {
                System.out.println("Sending buffered dns data count: " + this.dnsData.size());
                this.template.convertAndSend("/topic/dns-data", this.dnsData);
                this.dnsData.clear();
            }
            if (this.dnsDetections.size() > 0) {
                System.out.println("Sending buffered detections data count: " + this.dnsDetections.size());
                this.template.convertAndSend("/topic/dns-detection", this.dnsDetections);
                this.dnsDetections.clear();
            }
        }
    }
}
