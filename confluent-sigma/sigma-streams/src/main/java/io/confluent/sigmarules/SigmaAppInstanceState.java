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
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.kafka.streams.ThreadMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaAppInstanceState {

    final static Logger logger = LogManager.getLogger(SigmaAppInstanceState.class);

    private String applicationId;
    private String kafkaStreamsState;
    //private List<String> hosts;
    private Integer numRules;
    private Long sampleTimestamp;
    private List<Map<String,String>> threadMetadata;
    private String appHostName;
    public SigmaAppInstanceState()
    {
    }

    protected void popuplate(SigmaStream sigmaStreamApp) {
        sampleTimestamp = System.currentTimeMillis();
        try {
            appHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Unable to retrieve local host name for KafkaStreams app", e);
        }

        KafkaStreams kStreams = sigmaStreamApp.getStreams();

        setApplicationId(sigmaStreamApp.getApplicationId());
        KafkaStreams.State streamsState = kStreams.state();
        setKafkaStreamsState(streamsState.toString());
//        if (streamsState.isRunningOrRebalancing()) {
//            Collection<StreamsMetadata> metadataCollection =
//                    kStreams.metadataForAllStreamsClients();
//            List<String> hostList = new ArrayList<String>();
//            for (StreamsMetadata metadata : metadataCollection) {
//                hostList.add(metadata.hostInfo().toString());
//            }
//            setHosts(hostList);
//        } else {
//            setHosts(null);
//        }

        setNumRules(sigmaStreamApp.getRuleFactory().getSigmaRules().size());

        Set<ThreadMetadata> threadMedataSet = kStreams.metadataForLocalThreads();
        List<Map<String, String>> tmList = new ArrayList<>();

        for (ThreadMetadata tm : threadMedataSet)
        {
            Map<String, String> map = new HashMap<String,String>();
            map.put("clientId", tm.consumerClientId());
            map.put("threadState", tm.threadState());
            map.put("numTasks", String.valueOf(tm.activeTasks().size()));
            map.put("threadName", tm.threadName());

            tmList.add(map);
        }

        setThreadMetadata(tmList);
        //setHostName("foo");
    }

    /**
     * Get a key to use that uniquely identifies this sigma app instance state.
     * @return unique key
     */
    public String getKey()
    {
        return getApplicationId();
    }

//    public List<String> getHosts() {
//        return hosts;
//    }
//
//    public void setHosts(List<String> hosts) {
//        this.hosts = hosts;
//    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
