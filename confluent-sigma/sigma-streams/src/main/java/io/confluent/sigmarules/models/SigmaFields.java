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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaFields {
    private Map<String, Object> fieldmappings;

    public Map<String, Object> getFieldmappings() {
        return fieldmappings;
    }

    public void setFieldmappings(Map<String, Object> fieldmappings) {
        this.fieldmappings = fieldmappings;
    }

    public List<String> getSigmaField(String fieldName) {
        List<String> sigmaFields = new ArrayList<>();

        if (fieldmappings.containsKey(fieldName)) {
            Object fieldValue = fieldmappings.get(fieldName);
            if (fieldValue instanceof ArrayList) {
                List<Object> fieldList = (ArrayList)fieldValue;
                fieldList.forEach((f) -> {
                    sigmaFields.add(f.toString());
                });
            } else {
                sigmaFields.add(fieldValue.toString());
            }
        }

        return sigmaFields;
    }
}