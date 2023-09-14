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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.streams.SigmaStream;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.ThreadMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("rawtypes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaAppInstanceState {

    public static final String[] STRIPPED_PROPERTIES = new String[] {"security.protocol","sasl.jaas.config", "sasl.mechanism"};
    final static Logger logger = LogManager.getLogger(SigmaAppInstanceState.class);

    private String applicationId;
    private String kafkaStreamsState;
    private Integer numRules;
    private Long sampleTimestamp;
    private String sampleTimestampHr;
    private List<Map<String,String>> threadMetadata;
    private String appHostName;
    private Map appProperties;
    private String applicationInstanceId;
    private Integer recordsProcessed;
    private Integer numMatches;

    public SigmaAppInstanceState()
    {
    }

    protected void popuplate(SigmaStream sigmaStreamApp) {
        sampleTimestamp = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(sampleTimestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC"));
        sampleTimestampHr = instant.atZone(ZoneId.of("UTC")).format(formatter);

        try {
            appHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Unable to retrieve local host name for KafkaStreams app", e);
        }


        KafkaStreams kStreams = sigmaStreamApp.getStreams();
        setApplicationId(sigmaStreamApp.getApplicationId());
        setApplicationInstanceId(getApplicationId() + sigmaStreamApp.getInstanceId());
        KafkaStreams.State streamsState = kStreams.state();
        setKafkaStreamsState(streamsState.toString());

        setNumRules(sigmaStreamApp.getRuleFactory().getSigmaRules().size());
        setNumMatches(sigmaStreamApp.getNumMatches());
        setRecordsProcessed(sigmaStreamApp.getRecordsProcessed());
        
        Set<ThreadMetadata> threadMedataSet = kStreams.metadataForLocalThreads();
        List<Map<String, String>> tmList = new ArrayList<>();

        Properties props = (Properties) sigmaStreamApp.getStreamProperties().clone();

        // remove confidential data
        for (String key : STRIPPED_PROPERTIES) {
            props.remove(key);
        }

        // replace dots with underscores for proper JSON
        Properties updateProps = new Properties();
        props.forEach((key, value) -> {
            String newKey = key.toString().replace(".", "_");
            updateProps.setProperty(newKey, value.toString());
        });
        appProperties = updateProps;

        for (ThreadMetadata tm : threadMedataSet)
        {
            Map<String, String> map = new HashMap<>();
            map.put("clientId", tm.consumerClientId());
            map.put("threadState", tm.threadState());
            map.put("numTasks", String.valueOf(tm.activeTasks().size()));
            map.put("threadName", tm.threadName());

            tmList.add(map);
        }

        setThreadMetadata(tmList);
    }

    /**
     * Get a key to use that uniquely identifies this sigma app instance state.
     * @return unique key
     */
    public String getKey()
    {
        return getApplicationId();
    }


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public void setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
    }

    public Integer getNumRules() {
        return numRules;
    }

    public String getKafkaStreamsState() {
        return kafkaStreamsState;
    }

    public void setKafkaStreamsState(String kafkaStreamsState) {
        this.kafkaStreamsState = kafkaStreamsState;
    }

    public void setNumRules(Integer numRules) {
        this.numRules = numRules;
    }

    public Long getSampleTimestamp() {
        return sampleTimestamp;
    }

    public void setSampleTimestamp(Long sampleTimestamp) {
        this.sampleTimestamp = sampleTimestamp;
    }

    public List<Map<String, String>> getThreadMetadata() {
        return threadMetadata;
    }

    public void setThreadMetadata(List<Map<String, String>> threadMetadata) {
        this.threadMetadata = threadMetadata;
    }

    public String getAppHostName() {
        return appHostName;
    }

    public void setAppHostName(String appHostName) {
        this.appHostName = appHostName;
    }

    public Map getAppProperties() {
        return appProperties;
    }

    public void setAppProperties(Map appProperties) {
        this.appProperties = appProperties;
    }

    public String getSampleTimestampHr() {
        return sampleTimestampHr;
    }

    public void setSampleTimestampHr(String sampleTimestampHr) {
        this.sampleTimestampHr = sampleTimestampHr;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getNumMatches() {
        return numMatches;
    }

    public void setNumMatches(Integer numMatches) {
        this.numMatches = numMatches;
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static Serde<SigmaAppInstanceState> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", SigmaAppInstanceState.class);
        final Serializer<SigmaAppInstanceState> sigmaRuleSer = new KafkaJsonSerializer<>();
        sigmaRuleSer.configure(serdeProps, false);

        final Deserializer<SigmaAppInstanceState> sigmaRuleDes = new KafkaJsonDeserializer<>();
        sigmaRuleDes.configure(serdeProps, false);
        return Serdes.serdeFrom(sigmaRuleSer, sigmaRuleDes);
    }
}
