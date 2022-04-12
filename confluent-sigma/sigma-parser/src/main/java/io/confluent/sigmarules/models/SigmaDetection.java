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

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaDetection {
    final static Logger logger = LogManager.getLogger(SigmaDetection.class);

    private String name = ""; // TODO: This could be a list in the mapped file
    private String sigmaName = "";
    private OperatorType operator = null; //contains, beginswith, endswith
    private List<String> detectionValues = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return detectionValues;
    }

    public void addValue(String value) {
        this.detectionValues.add(value);
    }

    /**
     * For the given recordValue see if this detection matches.
     * @param recordValue the record value to check against the detection patterns
     * @param notCondition Is this match part of a not condition against the sigma detection patterns?
     * @return whether there is a match.
     */
    public boolean matches(String recordValue, Boolean notCondition) {
        if (!notCondition) {
            return matchDetectionPattern(recordValue);
        } else {
            // this is a not condition, need to verify all values do not match
            for (String detectionValue : detectionValues) {
                if (matchDetectionPattern(recordValue)) return false;
            }
        }
        return false;
    }

    /**
     * For the given pattern see if this record value matches.  A given detection can have multiple patterns so this is
     * called for each pattern in the detection
     * @param recordValue the record value to check against the detection patterns
     * @return whether there is a match.
     */
    private boolean matchDetectionPattern(String recordValue) {
        // if any value in the array is true, return and break out of the loop
        for (String detectionValue : detectionValues) {
            logger.debug("checking record value: " + recordValue + " against detectionValue: " + detectionValue);
            if (this.operator != null) {
                switch (this.operator) {
                    case GREATER_THAN:
                        return Long.parseLong(recordValue) > Long.parseLong(detectionValue) ;
                    case LESS_THAN:
                        return Long.parseLong(recordValue) < Long.parseLong(detectionValue) ;

                    //All these types have been converted into a regular expression by the parser.
                    case REGEX:
                    case STARTS_WITH:
                    case ENDS_WITH:
                        return recordValue.matches(detectionValue);
                    default:
                        // We should never get to a situation where there is an supported operator here since we should
                        // throw an exception during the creation of sigmaDetection  If we get here this a problem and
                        // we need to fail fast
                        throw new UnsupportedOperationException();
                }
            // TODO If there isn't a reason to treat the sigma detection pattern (or value or whatever we
            // want to call it) as a regex should we use matches?
            } else {
                return recordValue.matches(detectionValue);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DetectionModel [name=" + name + ", values=" + detectionValues + "]";
    }

    public OperatorType getOperator() {
        return operator;
    }

    public void setOperator(OperatorType operatorArg) {
        this.operator = operatorArg;
    }

    public void setSigmaName(String sigmaNameArg) {
        this.sigmaName = sigmaNameArg;
    }
}
