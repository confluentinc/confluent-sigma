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
package io.confluent.sigmarules.config;

/**
 * Enum for all the Sigma specific properties
 */
public enum SigmaPropertyEnum {
    BOOTSTRAP_SERVERS("bootstrap.servers", true, "localhost:9092"),
    SIGMA_RULES_TOPIC("sigma.rules.topic", true, "sigma-rules"),
    DATA_TOPIC("data.topic", true, "dns"),
    OUTPUT_TOPIC( "output.topic", true, "dns-detection"),
    APPLICATION_ID("application.id", true,"SigmaStreams"),
    SIGMA_RULE_FILTER_PRODUCT("sigma.rule.filter.product", false, null),
    SIGMA_RULE_FILTER_SERVICE("sigma.rule.filter.service", false, null),
    SIGMA_MAX_RULES("sigma.max.rules", false, null),
    FIELD_MAPPING_FILE("field.mapping.file", false, null),
    SIGMA_RULE_FILTER_TITLE("sigma.rule.filter.title", false, null),
    SIGMA_RULE_FILTER_LIST("sigma.rule.filter.list", false, null),
    SCHEMA_REGISTRY("schema.registry", false, "localhost:8081"),
    SECURITY_PROTOCOL("security.protocol", false, null),
    SASL_MECHANISM("sasl.mechanism", false, null),
    SIGMA_RULE_FIRST_MATCH("sigma.rule.first.match", false, null),
    SIGMA_APP_TOPIC("sigma.app.topic",false, "sigma-app-instances" ),
    SIGMA_APP_STATE_POLL_SLEEP("sigma.app.state.poll.sleep", false, "5000");

    private final String defaultValue;
    private final String name;
    private final boolean required;

    SigmaPropertyEnum(String nameArg, boolean requiredArg, String defaultArg) {
        this.name = nameArg;
        this.required = requiredArg;
        this.defaultValue = defaultArg;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

