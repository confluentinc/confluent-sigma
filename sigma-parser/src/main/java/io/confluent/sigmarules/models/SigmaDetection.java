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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaDetection {
    final static Logger logger = LogManager.getLogger(SigmaDetection.class);

    private String name = ""; // TODO: This could be a list in the mapped file
    private String sigmaName = "";
    private List<ModifierType> modifiers = new ArrayList<>(); //contains, beginswith, endswith
    private List<String> detectionValues = new ArrayList<>();
    private Map<String, String> regexMappedFields = new HashMap<>(); //groups from regex or other fields
    private Boolean matchAll = false;

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

    public Map<String, String> getRegexMappedFields() {
        return regexMappedFields;
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
        // if all is set, then all values must match otherwise,
        // if any value in the array is true, return and break out of the loop
        int matchCount = 0;
        for (String detectionValue : detectionValues) {
            if (logger.isDebugEnabled())
                logger.debug("checking record value: " + recordValue + " against detectionValue: "
                    + detectionValue);

            if (modifiers.size() > 0) {
                for (ModifierType modifier : modifiers) {
                    switch (modifier) {
                        case GREATER_THAN:
                            if (Long.parseLong(recordValue) > Long.parseLong(detectionValue))
                                matchCount++;
                            break;
                        case LESS_THAN:
                            if (Long.parseLong(recordValue) < Long.parseLong(detectionValue))
                                matchCount++;
                            break;
                        case REGEX:
                            if (checkRegexPattern(recordValue, detectionValue))
                                matchCount++;
                            break;
                        //All these types have been converted into a regular expression by the parser.
                        case CONTAINS:
                        case BEGINS_WITH:
                        case STARTS_WITH:
                        case ENDS_WITH:
                            if (recordValue.matches(detectionValue))
                                matchCount++;
                            break;
                        case ALL:
                            break;
                        default:
                            // We should never get to a situation where there is an supported operator here since we should
                            // throw an exception during the creation of sigmaDetection  If we get here this a problem and
                            // we need to fail fast
                            throw new UnsupportedOperationException();
                    }
                    // TODO If there isn't a reason to treat the sigma detection pattern (or value or whatever we
                    // want to call it) as a regex should we use matches?
                }
            } else if (recordValue.matches(detectionValue)) {
                matchCount++;
            }

            // if the modifier is set to ALL, then the matchCount must match the number of
            // detection values. otherwise, any match will return true
            if (matchAll) {
                if (matchCount == detectionValues.size()) {
                    return true;
                }
            } else if (matchCount > 0) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkRegexPattern(String recordValue, String detectionValue) {
        String[] names = StringUtils.substringsBetween(detectionValue, "<", ">");
        Pattern p = Pattern.compile(detectionValue);
        Matcher m = p.matcher(recordValue);

        if (m.find()) {
            // ...then you can use group() methods.
            if ((names != null) && (m.groupCount() > 0)) {
                for (int i = 0; i < names.length; i++) {
                    System.out.println(names[i] + ": " + m.group(names[i]));
                    regexMappedFields.put(names[i], m.group(names[i]));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "DetectionModel [name=" + name + ", values=" + detectionValues + "]";
    }

    public List<ModifierType> getModifiers() {
        return modifiers;
    }

    public void addModifier (ModifierType modifier) {
        if (modifier != null)
            this.modifiers.add(modifier);
    }

    public void setSigmaName(String sigmaNameArg) {
        this.sigmaName = sigmaNameArg;
    }

    public Boolean getMatchAll() {
        return matchAll;
    }

    public void setMatchAll(Boolean matchAll) {
        this.matchAll = matchAll;
    }

}
