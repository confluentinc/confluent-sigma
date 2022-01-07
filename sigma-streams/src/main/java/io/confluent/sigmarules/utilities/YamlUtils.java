package io.confluent.sigmarules.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlUtils {
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ObjectMapper getYAMLMapper() {
        return mapper;
    }
}
