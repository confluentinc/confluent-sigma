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

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.rules.DetectionsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SigmaCondition {
    final static Logger logger = LogManager.getLogger(SigmaCondition.class);

    private String conditionName = "";
    private String operator = "";
    private Boolean notCondition = false;
    private SigmaCondition pairedCondition = null;
    private Boolean aggregateCondition = false;

    public SigmaCondition(String conditionName) {
        this.conditionName = conditionName;
    }
    
    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public Boolean getNotCondition() {
        return notCondition;
    }

    public void setNotCondition(Boolean notCondition) {
        this.notCondition = notCondition;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public SigmaCondition getPairedCondition() {
        return pairedCondition;
    }

    public void setPairedCondition(SigmaCondition pairedCondition) {
        this.pairedCondition = pairedCondition;
    }

    public Boolean checkCondition(DetectionsManager detections, JsonNode sourceData) {
        if (!aggregateCondition) {
            Boolean pairedResult = false;
            if (pairedCondition != null) {
                pairedResult = pairedCondition.checkCondition(detections, sourceData);

                // if paired condition is true and operator is OR, no need to check
                if (pairedResult && operator.equals("OR")) {
                    return true;
                } else if (operator.equals("OR")) {
                    return checkMyConditions(detections, sourceData);
                } else { //AND case
                    Boolean myResult = checkMyConditions(detections, sourceData);
                    return pairedResult && myResult;
                }
            } else {
                return checkMyConditions(detections, sourceData);
            }
        }

        return false;
    }

    private Boolean checkMyConditions(DetectionsManager detections, JsonNode sourceData) {
        // detections grouped together are combined for a status
        Boolean validDetections = false;
        SigmaDetectionList myDetections = detections.getDetectionsByName(conditionName);

        if (myDetections != null) {
            for (SigmaDetection d : myDetections.getDetections()) {
                String name = d.getName();
                if (sourceData.has(name)) {
                    JsonNode sourceValues = sourceData.get(name);

                    if (sourceValues.isArray()) {
                        for (final JsonNode sourceValue : sourceValues) {
                            if (!checkValue(d, sourceValue.toString())) {
                                return false;
                            } else {
                                validDetections = true;
                            }
                        }
                    } else {
                        // this is a string
                        String sourceValue = sourceValues.toString();
                        if (!checkValue(d, sourceValue)) {
                            return false;
                        } else {
                            validDetections = true;
                        }
                    }
                } else {
                    // does not contain all detection names
                    return false;
                }
            }

            if (validDetections) {
                return true;
            }
        } else {
            logger.info("No detections for condition: " + conditionName + " source: " + sourceData.toString());
        }

        return false;
    }

    private Boolean checkValue(SigmaDetection model, String sourceValue) {
        if (model.matches(sourceValue, this.notCondition)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String condition = new String(conditionName);

        if (operator != null) {
            condition += " " + operator;
        }

        if (notCondition == true) {
            condition += " NOT"; 
        }

        if (pairedCondition != null) {
            condition += " " + pairedCondition.toString();
        }

        return condition;
    }

    public Boolean getAggregateCondition() {
        return aggregateCondition;
    }

    public void setAggregateCondition(Boolean aggregateCondition) {
        this.aggregateCondition = aggregateCondition;
    }
}
