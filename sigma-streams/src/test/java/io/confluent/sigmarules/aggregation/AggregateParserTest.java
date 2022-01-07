package io.confluent.sigmarules.aggregation;

import io.confluent.sigmarules.models.AggregateValues;
import io.confluent.sigmarules.parsers.AggregateParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AggregateParserTest {

    @Test
    void parseCondition() {
        AggregateParser parser = new AggregateParser();
        AggregateValues results = parser.parseCondition("count() > 15");
        assertTrue(results.getOperation() != null);
    }
}