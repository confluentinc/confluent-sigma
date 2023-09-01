package io.confluent.sigmarules.config;

import java.util.Properties;
import io.kcache.KafkaCacheConfig;

public class KcacheConfig {
    public static final String KEY_CONVERTER_SCHEMA_REGISTRY_URL = "key.converter.schema.registry.url";
    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";

    public static Properties createConfig(Properties properties, SigmaPropertyEnum topic) {
        Properties kcacheProps = new Properties(properties);
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG,
                properties.getProperty(SigmaPropertyEnum.BOOTSTRAP_SERVERS.toString()));

        String sigmaAppTopic = properties.getProperty(topic.toString());
        if (sigmaAppTopic == null) sigmaAppTopic = topic.getDefaultValue();
        kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, sigmaAppTopic);

        // optional config parameters
        if (properties.containsKey(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SECURITY_PROTOCOL_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SECURITY_PROTOCOL.toString()));

        if (properties.containsKey(SigmaPropertyEnum.SASL_MECHANISM.toString()))
            kcacheProps.setProperty(KafkaCacheConfig.KAFKACACHE_SASL_MECHANISM_CONFIG,
                    properties.getProperty(SigmaPropertyEnum.SASL_MECHANISM.toString()));

        if (properties.containsKey("sasl.jaas.config"))
            kcacheProps.setProperty("kafkacache.sasl.jaas.config",
                properties.getProperty("sasl.jaas.config"));

        if (properties.containsKey("sasl.client.callback.handler.class"))
            kcacheProps.setProperty("kafkacache.sasl.client.callback.handler.class",
                    properties.getProperty("sasl.client.callback.handler.class"));

        if (properties.containsKey(SigmaPropertyEnum.SCHEMA_REGISTRY.toString())) {
            kcacheProps.setProperty(KEY_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
            kcacheProps.setProperty(VALUE_CONVERTER_SCHEMA_REGISTRY_URL,
                properties.getProperty(SigmaPropertyEnum.SCHEMA_REGISTRY.toString()));
        }

        return kcacheProps;
    }
}
