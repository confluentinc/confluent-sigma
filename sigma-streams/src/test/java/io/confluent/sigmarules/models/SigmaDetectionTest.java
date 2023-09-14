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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.parsers.SigmaRuleParser;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SigmaDetectionTest {

    @Test
    void testExactMatch() {
        SigmaDetection detection = new SigmaDetection();
        detection.addValue("foo");
        assertTrue(detection.matches("foo", false));
        assertFalse(detection.matches("zork", false));
    }

    @Test
    void testTrailingStarNoOperator()
        throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo: 'ab*'\n"
            + "  condition: test";

        SigmaRuleParser ruleParser = new SigmaRuleParser();
        SigmaRule sigmaRule = ruleParser.parseRule(testRule);

        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("abcXXXXX", false));
        assertFalse(detection.matches("fooabfoo", false));
        assertFalse(detection.matches("YYYYY", false));
    }

    @Test
    void testRegex() throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: 'ab.*'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);

        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("abbb", false));
        assertFalse(detection.matches("a", false));
        assertTrue(detection.matches("ab", false));
        assertTrue(detection.matches("abc", false));

        String testRule2 = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: 'ab.*c'\n"
            + "  condition: test";

        SigmaRule sigmaRule2 = ruleParser.parseRule(testRule2);

        SigmaDetections detections2 = sigmaRule2.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection2 = detections2.getDetections().get(0);
        assertFalse(detection2.matches("abbb", false));
        assertTrue(detection2.matches("abbbc", false));
        assertFalse(detection2.matches("ac", false));
        assertTrue(detection2.matches("abbbcL", false));
    }

    @Test
    void testRegexBrackets() throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: 'a[xy]c'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);

        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("ayc", false));
        assertTrue(detection.matches("axc", false));
        assertFalse(detection.matches("abc", false));
    }

    @Test
    void testRegexCurlyBraces()
        throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: 'a{2}b'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);

        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("aab", false));
        assertFalse(detection.matches("ab", false));
    }

    @Test
    void testRegexSpecial() throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: 'a.b'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);
        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("aXb", false));
        assertFalse(detection.matches("ab", false));

        String testRule2 = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|re: '^a.b$'\n"
            + "  condition: test";

        SigmaRule sigmaRule2 = ruleParser.parseRule(testRule2);
        SigmaDetections detections2 = sigmaRule2.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection2 = detections2.getDetections().get(0);
        assertTrue(detection2.matches("aXb", false));
        assertFalse(detection2.matches("ab", false));
    }

    @Test
    void testStartsWith() throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|startswith: 'a'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);
        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("aXb", false));
        assertTrue(detection.matches("a", false));
        assertFalse(detection.matches("bx", false));
    }

    @Test
    void testEndsWith() throws InvalidSigmaRuleException, IOException, SigmaRuleParserException {
        SigmaRuleParser ruleParser = new SigmaRuleParser();

        String testRule = "title: Simple Http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo|endswith: 'b'\n"
            + "  condition: test";

        SigmaRule sigmaRule = ruleParser.parseRule(testRule);
        SigmaDetections detections = sigmaRule.getDetectionsManager().getDetectionsByName("test");
        SigmaDetection detection = detections.getDetections().get(0);
        assertTrue(detection.matches("ab", false));
        assertTrue(detection.matches("b", false));
        assertFalse(detection.matches("aa", false));
    }

 }