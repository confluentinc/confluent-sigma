package io.confiuent.sigmaui.controllers;

import io.confiuent.sigmaui.models.DNSStreamData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;

import io.confiuent.sigmaui.models.DetectionResults;
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
        }
    }

    @KafkaListener(topics = {"dns-detection"}, containerFactory = "dnsDetectionKafkaListenerContainerFactory")
    public void consumeDetectionData(DetectionResults results) {
        synchronized (this.dnsDetections) {
            this.dnsDetections.add(results);
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
