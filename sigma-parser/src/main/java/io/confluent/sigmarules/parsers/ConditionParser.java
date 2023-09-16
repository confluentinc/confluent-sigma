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

package io.confluent.sigmarules.parsers;

import io.confluent.sigmarules.models.ConditionsManager;
import io.confluent.sigmarules.models.SigmaCondition;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConditionParser {
    final static Logger logger = LogManager.getLogger(ConditionParser.class);

    static final String OPEN_PAREN = "(";
    static final String CLOSE_PAREN = ")";
    static final String NOT = "NOT"; //ignore case
    static final String AND = "AND"; //ignore case
    static final String OR = "OR"; //ignore case
    static final String SPACE = " ";
    static final String AGG_SEP = "|";
    static Boolean inConditionStatement = false;
    static Boolean notCondition = false;

    private String tempString = "";
    private SigmaCondition currentCondition = null;;
    private AggregateParser aggregateParser = new AggregateParser();

    public ConditionsManager parseCondition(ParsedSigmaRule sigmaRule) {
        ConditionsManager conditionsManager = new ConditionsManager();

        if (sigmaRule.getDetection().containsKey("condition")) {
            String condition = sigmaRule.getDetection().get("condition").toString();

            List<SigmaCondition> conditions = conditionsManager.getConditions();
            CharacterIterator it = new StringCharacterIterator(condition);

            Boolean doneParsing = false;
            while (!doneParsing && it.current() != CharacterIterator.DONE) {
                //System.out.println(it.current());
                String currentChar = Character.toString(it.current());
                switch (currentChar) {
                    case OPEN_PAREN:
                        logger.debug("OPEN");
                        break;
                    case CLOSE_PAREN:
                        evaluateString(conditions, tempString);
                        logger.debug("CLOSE");
                        break;
                    case SPACE:
                        if (!tempString.isBlank()) {
                            evaluateString(conditions, tempString);
                        }
                        break;
                    case AGG_SEP:
                        // aggregate condition
                        String aggString = StringUtils.substringAfter(condition, "| ");
                        SigmaCondition aggregateCondition = new SigmaCondition(aggString);
                        aggregateCondition.setAggregateCondition(true);
                        aggregateCondition.setAggregateValues(aggregateParser.parseCondition(aggString));
                        conditions.add(aggregateCondition);
                        doneParsing = true;
                        break;
                    default:
                        tempString = tempString.concat(currentChar);
                        break;
                }

                it.next();
            }

            evaluateString(conditions, tempString);
        }

        return conditionsManager;
    }

    private void evaluateString(List<SigmaCondition> conditions, String eval) {
        if (!tempString.isBlank()) {
            String operatorEval = eval.toUpperCase();
            switch (operatorEval) {
                case AND:
                case OR:
                    currentCondition.setOperator(eval);
                    inConditionStatement = true;
                    break;
                case NOT:
                    logger.debug("this is a not statement");
                    notCondition = true;
                    inConditionStatement = true;
                    break;
                case OPEN_PAREN:
                case CLOSE_PAREN:
                    //skipping for now
                    break;
                default:
                    // if in a condition statement, must be 2nd parameter
                    if (currentCondition != null && inConditionStatement == true) {
                        SigmaCondition newCondition = new SigmaCondition(eval);
                        if (notCondition) {
                            logger.debug("setting not condition");
                            newCondition.setNotCondition(true);
                            notCondition = false;
                        }
                        currentCondition.setPairedCondition(newCondition);

                        // set the current condition to the new condition
                        currentCondition = newCondition;
                        inConditionStatement = false;
                    } else {
                        currentCondition = new SigmaCondition(eval);
                        if (notCondition) {
                            logger.debug("setting not condition");
                            currentCondition.setNotCondition(true);
                            notCondition = false;
                        }
                        conditions.add(currentCondition);
                    }

            }
            tempString = "";
        }
    }

}
