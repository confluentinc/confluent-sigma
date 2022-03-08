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

package io.confluent.sigmarules.models;

import io.confluent.sigmarules.rules.SigmaRuleManager;

public class AggregateInfo {
    private String streamName;
    private SigmaRuleManager ruleManager;
    private String conditionStatement;
    private String inputTopic;
    private String outputTopic;
    private Long windowTimeMS = 0L;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public SigmaRuleManager getRuleManager() {
        return ruleManager;
    }

    public void setRuleManager(SigmaRuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public String getConditionStatement() {
        return conditionStatement;
    }

    public void setConditionStatement(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    public Long getWindowTimeMS() {
        return windowTimeMS;
    }

    public void setWindowTimeMS(Long windowTimeMS) {
        this.windowTimeMS = windowTimeMS;
    }
}
