package io.confiuent.sigmaui.rules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.confiuent.sigmaui.models.SigmaRule;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.kcache.Cache;
import io.kcache.CacheUpdateHandler;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component
public class SigmaRulesStore implements CacheUpdateHandler<String, SigmaRule> {
    static final Logger logger = LogManager.getLogger(io.confiuent.sigmaui.rules.SigmaRulesStore.class);

    private Cache<String, SigmaRule> sigmaRulesCache;

    private SigmaRuleObserver observer = null;

    @Value("${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value("${kafka.schemaRegistry}")
    private String schemaRegistry;

    @PostConstruct
    private void initialize() {
        logger.info("bootstrap address: " + this.bootstrapAddress);
        Properties props = new Properties();
        props.setProperty("kafkacache.bootstrap.servers", this.bootstrapAddress);
        props.setProperty("kafkacache.topic", "sigma-rules");
        props.setProperty("key.converter.schema.registry.url", this.schemaRegistry);
        props.setProperty("value.converter.schema.registry.url", this.schemaRegistry);
        this.sigmaRulesCache = (Cache<String, SigmaRule>)new KafkaCache(new KafkaCacheConfig(props), Serdes.String(), getJsonSerde(), this, null);
        this.sigmaRulesCache.init();
    }

    public void addObserver(SigmaRuleObserver observer) {
        this.observer = observer;
    }

    public static Serde<SigmaRule> getJsonSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", SigmaRule.class);
        KafkaJsonSerializer kafkaJsonSerializer = new KafkaJsonSerializer();
        kafkaJsonSerializer.configure(serdeProps, false);
        KafkaJsonDeserializer kafkaJsonDeserializer = new KafkaJsonDeserializer();
        kafkaJsonDeserializer.configure(serdeProps, false);
        return Serdes.serdeFrom((Serializer)kafkaJsonSerializer, (Deserializer)kafkaJsonDeserializer);
    }

    public void addRule(String ruleName, String rule) {
        ObjectMapper mapper = new ObjectMapper((JsonFactory)new YAMLFactory());
        SigmaRule sigmaRule = null;
        try {
            if (rule != null) {
                sigmaRule = (SigmaRule)mapper.readValue(rule, SigmaRule.class);
                this.sigmaRulesCache.put(ruleName, sigmaRule);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void removeRule(String ruleName) {
        this.sigmaRulesCache.remove(ruleName);
    }

    public Set<String> getRuleNames() {
        return this.sigmaRulesCache.keySet();
    }

    public String getRuleAsYaml(String ruleName) {
        return ((SigmaRule)this.sigmaRulesCache.get(ruleName)).toString();
    }

    Cache<String, SigmaRule> getRules() {
        return this.sigmaRulesCache;
    }

    public SigmaRule getRule(String ruleName) {
        ObjectMapper mapper = new ObjectMapper((JsonFactory)new YAMLFactory());
        SigmaRule sigmaRule = null;
        try {
            String rule = getRuleAsYaml(ruleName);
            if (rule != null)
                sigmaRule = (SigmaRule)mapper.readValue(rule, SigmaRule.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return sigmaRule;
    }

    public void handleUpdate(String key, SigmaRule value, SigmaRule oldValue, TopicPartition tp, long offset, long timestamp) {
        if ((oldValue == null || (oldValue != null && !value.equals(oldValue))) &&
                this.observer != null)
            this.observer.handleRuleUpdate(key, getRuleAsYaml(key));
    }

    public static void main(String[] args) {}
}
