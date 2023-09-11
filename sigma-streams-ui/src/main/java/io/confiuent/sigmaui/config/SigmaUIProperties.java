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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SigmaUIProperties {
    private Properties properties = new Properties();

    @PostConstruct
    private void initialize() {
        if (isDockerized()) {
            System.out.println("Initialize from environment variables");
            properties = getPropertiesFromEnv();
        } else {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                properties.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDockerized() {
        File f = new File("/.dockerenv");
        return f.exists();
    }

    private Properties getPropertiesFromEnv() {
        Properties props = new Properties();
        System.getenv().forEach((k, v) -> {
            String newKey = k.replace("_", ".");
            System.out.println(newKey + ": " + v);
            props.setProperty(newKey, v);
        });

        return props;
    }

    public Properties getProperties() {
        return this.properties;
    }
}
