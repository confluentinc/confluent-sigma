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

package io.confluent.sigmarules.streams;

import io.confluent.sigmarules.models.AggregateInfo;
import io.confluent.sigmarules.rules.SigmaRuleManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AggregateStreamsFactory {
    private Map<String, AggregateStream> streams = new HashMap<>();
    private Properties properties;

    public AggregateStreamsFactory(Properties properties) {
        this.properties = properties;
    }

    public void createAggregateStream(String streamName, SigmaRuleManager ruleManager) {
        if(streams.containsKey(streamName) == false) {
            String inputTopic = properties.getProperty("data.topic");
            String outputTopic = properties.getProperty("output.topic");

            inputTopic += "-agg-" + streamName.hashCode();
            AggregateInfo aggregateInfo = new AggregateInfo();
            aggregateInfo.setStreamName(inputTopic);
            aggregateInfo.setRuleManager(ruleManager);
            aggregateInfo.setConditionStatement(ruleManager.getConditions().getAggregateCondition());
            aggregateInfo.setInputTopic(inputTopic);
            aggregateInfo.setOutputTopic(outputTopic);
            aggregateInfo.setWindowTimeMS(ruleManager.getWindowTimeMS());

            AggregateStream newStream = new AggregateStream(properties, aggregateInfo);
            newStream.startStream();
            streams.put(streamName, newStream);
        }
    }
}
