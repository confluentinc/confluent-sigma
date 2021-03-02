package io.confluent.sigmarules;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import io.confluent.sigmarules.models.SigmaFSConnector;
import io.confluent.sigmarules.models.SigmaRule;

public class SigmaRawStream {
    private Properties config; 
    private KafkaStreams streams;
    private SigmaRulesManager sigmaRulesManager;

    public SigmaRawStream(SigmaRulesManager sigmaRulesManager) {
        this.config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "sigma-rules-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // create the rules cache
        this.sigmaRulesManager = sigmaRulesManager;

    }

    private StreamsBuilder createBuilder() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sigmaRulesRaw = builder.stream("sigma_rules_raw");
        KStream<String, SigmaRule> sigmaRule = sigmaRulesRaw.mapValues(v -> {
            return createSigmaRule(v);
        });
        // sigmaRule.to("sigma_rule", Produced.with(Serdes.String(), Serdes.String()));

        return builder;
    }

    public void startStream() {
        this.streams = new KafkaStreams(createBuilder().build(), config);
        streams.cleanUp();
        streams.start();
 

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private SigmaRule createSigmaRule(String rule) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SigmaRule sigmaRule = null;
        try {
            SigmaFSConnector sigmaFSConnector = mapper.readValue(rule, SigmaFSConnector.class);
            sigmaRule = sigmaFSConnector.getPayload();
            System.out.println("title: " + sigmaRule.getTitle());

            // add the rule to the cache
            sigmaRulesManager.addRule(sigmaRule.getTitle(), rule);

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return sigmaRule;
    }


    public static void main(String[] args) {
        // TODO: add config as arguments
        SigmaRawStream sigma = new SigmaRawStream(new SigmaRulesManager("127.0.0.1:9092"));
        sigma.startStream();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }

    }


}
