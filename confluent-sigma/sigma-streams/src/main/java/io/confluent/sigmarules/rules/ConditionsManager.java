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

package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.parsers.ConditionParser;
import io.confluent.sigmarules.models.SigmaCondition;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ConditionsManager {
    final static Logger logger = LogManager.getLogger(ConditionsManager.class);
    private List<SigmaCondition> conditions = new ArrayList<>();

    public List<SigmaCondition> getConditions() {
        return this.conditions;
    }

    public void addCondition(SigmaCondition condition) {
        conditions.add(condition);
    }

    public Boolean checkConditions(DetectionsManager detections, JsonNode sourceData) {
        for (SigmaCondition c : conditions) {
            if (!c.getAggregateCondition()) {
                if (c.checkCondition(detections, sourceData)) {
                    logger.debug("source data: " + sourceData.toString());
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean hasAggregateCondition() {
        for (SigmaCondition c : conditions) {
            if (c.getAggregateCondition()) {
                return true;
            }
        }

        return false;
    }

    public String getAggregateCondition() {
        for (SigmaCondition c : conditions) {
            if (c.getAggregateCondition()) {
                return c.getConditionName();
            }
        }

        return null;
    }
}
