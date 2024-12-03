package io.confluent.sigmarules.processor.avro;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.confluent.sigmarules.processor.SourceProcessor;

public class AvroSourceProcessor  implements SourceProcessor {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public KStream<String, JsonNode> createSourceProcessor(StreamsBuilder builder, String inputTopic) {
        KStream<String, GenericRecord> avroStream = builder.stream(inputTopic);
        return avroStream.mapValues(sourceData -> avroToJson(sourceData));
    }

    public JsonNode avroToJson(GenericRecord genericSource) {
        try {
            return mapper.readTree(genericSource.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
