package io.confluent.sigmarules.utilities;

import io.confluent.sigmarules.SigmaStreamsApp;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SigmaProperties {
    final static Logger logger = LogManager.getLogger(SigmaProperties.class);

    private Properties properties = new Properties();

    public SigmaProperties() {

    }

    public SigmaProperties(String[] args) {
        Options options = new Options();
        setOptions(options);

        parseArgs(args);
    }

    private void setOptions(Options options) {
        options.addOption("c", "config", true, "Path to properties file");
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
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("sigma_app", options, true);

                System.exit(0);
            }
        } catch (ParseException | FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String property) throws IllegalArgumentException {
        if (properties.containsKey(property) == false) {
            logger.fatal("Properties file does not contain " + property);
            throw new IllegalArgumentException(property + " not in properties file");
        }

        return properties.getProperty(property);
    }
}
