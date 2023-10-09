package io.confluent.sigmarules.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SigmaOptionsTest {

    @Test
    void hasAllRequiredPropertiesComplete() {
        Properties props = new Properties();
        for (SigmaPropertyEnum sigmaProp: SigmaPropertyEnum.values())
            props.setProperty(sigmaProp.getName(),"foo");
        SigmaOptions sigmaOptions = new SigmaOptions(props);
        assertTrue(sigmaOptions.hasAllRequiredProperties(true));
    }
    @Test
    void hasAllRequiredPropertiesMissing() {
        SigmaOptions sigmaOptions = new SigmaOptions(new Properties());
        assertFalse(sigmaOptions.hasAllRequiredProperties(false));
    }


}