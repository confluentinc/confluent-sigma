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

package io.confiuent.sigmaui.config;

import java.util.Properties;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SigmaUIProperties {
    private Properties properties = new Properties();

    @Value("${bootstrap.server}")
    private String bootstrapAddress;

    @Value("${schema.registry}")
    private String schemaRegistry;

    @Value("${sigma.rules.topic}")
    private String rulesTopic;

    @Value("${security.protocol:}")
    private String securityProtocol;

    @Value("${sasl.mechanism:}")
    private String saslMechanism;

    @Value("${sasl.jaas.config:}")
    private String saslJaasConfig;

    @PostConstruct
    private void initialize() {
        properties.setProperty("bootstrap.server", bootstrapAddress);
        properties.setProperty("schema.registry", schemaRegistry);
        properties.setProperty("sigma.rules.topic", rulesTopic);

        if (!securityProtocol.isEmpty())
            properties.setProperty("security.protocol", securityProtocol);

        if (!saslMechanism.isEmpty())
            properties.setProperty("sasl.mechanism", saslMechanism);

        if (!saslJaasConfig.isEmpty())
            properties.setProperty("sasl.jaas.config", saslJaasConfig);
    }

    public Properties getProperties() {
        return this.properties;
    }
}
