package io.confluent.sigmarules.processor;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import com.fasterxml.jackson.databind.JsonNode;

public interface SourceProcessor {
    public KStream<String, JsonNode> createSourceProcessor(StreamsBuilder builder, String inputTopic);

}
