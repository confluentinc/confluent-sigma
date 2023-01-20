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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaCondition {
    final static Logger logger = LogManager.getLogger(SigmaCondition.class);

    private String conditionName = "";
    private String operator = "";
    private Boolean notCondition = false;
    private SigmaCondition pairedCondition = null;
    private Boolean aggregateCondition = false;
    private AggregateValues aggregateValues = null;

    public SigmaCondition() {}

    public SigmaCondition(String conditionName) {
        this.conditionName = conditionName;
    }

    public AggregateValues getAggregateValues() {
        return aggregateValues;
    }

    public void setAggregateValues(AggregateValues aggregateValues) {
        this.aggregateValues = aggregateValues;
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

    public Boolean getAggregateCondition() {
        return aggregateCondition;
    }

    public void setAggregateCondition(Boolean aggregateCondition) {
        this.aggregateCondition = aggregateCondition;
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
}
