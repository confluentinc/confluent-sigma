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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaOptions {

    final static Logger logger = LogManager.getLogger(SigmaOptions.class);

    private Properties properties = new Properties();

    public SigmaOptions() {
    }

    public SigmaOptions(String[] args) {
        parseArgs(args);
    }

    private void setOptions(Options options) {
        options.addOption("c", "config", true, "Path to properties file");
        options.addOption("f", "file", true, "Path to sigma rule file.");
        options.addOption("d", "dir", true, "Path to directory contain sigma rules.");
        options.addOption("h", "headless", false,"If set then all required properties " +
                "must be available or else the application will exit with a help message.  In the absence of this " +
                "option the application will interactively prompt for any required properties that are not present.");
        options.addOption("?", "help", false, "Command line help");
    }

    public void parseArgs(String[] args) {
        Options options = new Options();
        setOptions(options);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {
                InputStream input = new FileInputStream(cmd.getOptionValue("c"));
                properties.load(input);
            }
            if (!hasAllRequiredProperties()) {
                if (cmd.hasOption("h")) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("sigma_app", options, true);
                    System.exit(0);
                } else interrogateProperties();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private boolean hasAllRequiredProperties()
    {
        for (SigmaPropertyEnum sigmaProp: SigmaPropertyEnum.values())
            if (sigmaProp.isRequired())
                if (properties.getProperty(sigmaProp.getName()) == null) return false;

        return true;
    }

    private void interrogateProperties() {
        Scanner scanner = new Scanner (System.in);

        for (SigmaPropertyEnum sigProp :SigmaPropertyEnum.values())
        {
            if (sigProp.isRequired())
                if (properties.getProperty(sigProp.getName()) == null ||
                        properties.getProperty(sigProp.getName()).isEmpty()) {
                    System.out.print(sigProp.getName() + ": <" + sigProp.getDefaultValue() + "> : ");
                    String line = scanner.nextLine();

                    if (line == null || line.isEmpty())
                        properties.setProperty(sigProp.getName(), sigProp.getDefaultValue());
                    else
                        properties.setProperty(sigProp.getName(), line);
                }
        }
        logger.info("Properties after interrogation: " + properties.toString());
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String property) throws IllegalArgumentException {
        if (!properties.containsKey(property)) {
            logger.fatal("Properties file does not contain " + property);
            throw new IllegalArgumentException(property + " not in properties file");
        }

        return properties.getProperty(property);
    }

    public static void main(String[] args)
    {
        SigmaOptions options = new SigmaOptions();
        options.parseArgs(args);
    }
}
