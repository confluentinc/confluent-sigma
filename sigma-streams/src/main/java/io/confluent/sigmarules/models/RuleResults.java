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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class RuleResults {
    private String title;
    private String id;
    private Boolean isAggregateCondition = false;
    private String outputTopic;
    private Map<String, String> customFields = new HashMap<>();

    public Boolean getIsAggregateCondition() {
        return isAggregateCondition;
    }
    public void setIsAggregateCondition(Boolean isAggregateCondition) {
        this.isAggregateCondition = isAggregateCondition;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Boolean getAggregateCondition() {
        return isAggregateCondition;
    }

    public void setAggregateCondition(Boolean aggregateCondition) {
        isAggregateCondition = aggregateCondition;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    @JsonAnyGetter 
    public Map<String, String> getCustomFields() {
        return customFields;
    }

    @JsonAnySetter 
    public void addCustomField(String key, String value) {
        this.customFields.put(key, value); 
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    
}
