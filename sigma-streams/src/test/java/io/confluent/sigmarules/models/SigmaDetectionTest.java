package io.confluent.sigmarules.models;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.parsers.DetectionParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SigmaDetectionTest {

    @Test
    void testExactMatch() {
        SigmaDetection detection = new SigmaDetection();
        detection.addValue("foo");
        assertTrue(detection.matches("foo", false));
        assertFalse(detection.matches("zork", false));
    }

    @Test
    void testTrailingStarNoOperator() throws InvalidSigmaRuleException {
        // Probably need to do a better job of no relying on DetectionParse but it seems hard to test the spirit
        // of the SigmaDetection with out using the combination of the too otherwise.
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo=ab*}");
        assertTrue(detection.matches("abcXXXXX", false));
        assertFalse(detection.matches("fooabfoo", false));
        assertFalse(detection.matches("YYYYY", false));
    }

    @Test
    void testRegex() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|re=ab*}");
        assertTrue(detection.matches("abbb", false));
        assertTrue(detection.matches("a", false));
        assertTrue(detection.matches("ab", false));
        assertFalse(detection.matches("abc", false));

        detection = parser.parseDetection("{foo|re=ab*c}");
        assertFalse(detection.matches("abbb", false));
        assertTrue(detection.matches("abbbc", false));
        assertTrue(detection.matches("ac", false));
        assertFalse(detection.matches("abbbcL", false));
    }

    @Test
    void testRegexBrackets() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();

        SigmaDetection detection = parser.parseDetection("{foo|re=a[xy]c}");
        assertTrue(detection.matches("ayc", false));
        assertTrue(detection.matches("axc", false));
        assertFalse(detection.matches("abc", false));
    }

    @Test
    void testRegexCurlyBraces() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|re=a{2}b}");
        assertTrue(detection.matches("aab", false));
        assertFalse(detection.matches("ab", false));
    }

    @Test
    void testRegexSpecial() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|re=a.b}");
        assertTrue(detection.matches("aXb", false));
        assertFalse(detection.matches("ab", false));

        detection = parser.parseDetection("{foo|re=^a.b$}");
        assertTrue(detection.matches("aXb", false));
        assertFalse(detection.matches("ab", false));
    }

    @Test
    void testStartsWith() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|startswith=a}");
        assertTrue(detection.matches("aXb", false));
        assertTrue(detection.matches("a", false));
        assertFalse(detection.matches("bx", false));
    }

    @Test
    void testEndsith() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|endswith=b}");
        assertTrue(detection.matches("ab", false));
        assertTrue(detection.matches("b", false));
        assertFalse(detection.matches("aa", false));
    }

    @Test
    void testGreater() throws InvalidSigmaRuleException {
        DetectionParser parser = new DetectionParser();
        SigmaDetection detection = parser.parseDetection("{foo|endswith=b}");
        assertTrue(detection.matches("ab", false));
        assertTrue(detection.matches("b", false));
        assertFalse(detection.matches("aa", false));
    }
}