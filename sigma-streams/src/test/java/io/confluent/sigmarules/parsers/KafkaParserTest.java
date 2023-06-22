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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.confluent.sigmarules.exceptions.InvalidSigmaRuleException;
import io.confluent.sigmarules.exceptions.SigmaRuleParserException;
import io.confluent.sigmarules.models.SigmaRule;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class KafkaParserTest {

    @Test
    void parseKafkaRule() throws IOException, InvalidSigmaRuleException, SigmaRuleParserException {
        String testRule = "title: Simple Http\n"
            + "logsource:\n"
            + "  product: zeek\n"
            + "  service: http\n"
            + "detection:\n"
            + "  test:\n"
            + "   - foo: 'ab*'\n"
            + "  condition: test\n"
            + "kafka:\n"
            + "  outputTopic: my-test-results";

        SigmaRuleParser parser = new SigmaRuleParser();
        SigmaRule rule = parser.parseRule(testRule);

        assertTrue(rule.getKafkaRule().getOutputTopic().matches("my-test-results"));
    }
}