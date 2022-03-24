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
package io.confluent.sigmarules;

/**
 * Enum for all the Sigma specific properties
 */
public enum SigmaProperties {

    BOOTSTRAP_SERVER("bootstrap.server", true),
    SIGMA_RULES_TOPIC("sigma.rules.topic", true),
    DATA_TOPIC("data.topic", true),
    OUTPUT_TOPIC( "output.topic", true),
    SIGMA_RULE_FILTER_PRODUCT("sigma.rule.filter.product", false),
    SIGMA_RULE_FILTER_SERVICE("sigma.rule.filter.service", false),
    SIGMA_MAX_RULES("sigma.max.rules", false),
    FIELD_MAPPING_FILE("field.mapping.file", false),
    SIGMA_RULE_FILTER_TITLE("sigma.rule.filter.title", false),
    SIGMA_RULE_FILTER_LIST("sigma.rule.filter.list", false),
    SCHEMA_REGISTRY("schema.registry", false),
    SECURITY_PROTOCOL("security.protocol", false),
    SASL_MECHANISM("sasl.mechanism", false);


    private final String name;
    private final boolean required;

    SigmaProperties(final String nameArg, boolean requiredArg) {
        this.name = nameArg;
        this.required = requiredArg;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
