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

package io.confiuent.sigmaui.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class RegexRule {
    private String sourceType;
    private String regex;
    private String outputTopic;
    private Map<String, String> customFields = new HashMap<>();

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RegexRule fromString(String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(value, RegexRule.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

    }
}
