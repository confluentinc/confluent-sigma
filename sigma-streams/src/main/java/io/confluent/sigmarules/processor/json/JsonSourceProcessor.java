package io.confluent.sigmarules.processor.json;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import com.fasterxml.jackson.databind.JsonNode;

import io.confluent.sigmarules.processor.SourceProcessor;
import io.confluent.sigmarules.utilities.JsonUtils;

public class JsonSourceProcessor  implements SourceProcessor {
    
    @Override
    public KStream<String, JsonNode> createSourceProcessor(StreamsBuilder builder, String inputTopic) {
        return builder.stream(inputTopic, Consumed.with(Serdes.String(), JsonUtils.getJsonSerde()));
    }

}
