package io.confluent.sigmarules.processor;

import io.confluent.sigmarules.config.TopicFormatEnum;
import io.confluent.sigmarules.processor.avro.AvroSinkProcessor;
import io.confluent.sigmarules.processor.avro.AvroSourceProcessor;
import io.confluent.sigmarules.processor.json.JsonSinkProcessor;
import io.confluent.sigmarules.processor.json.JsonSourceProcessor;
import io.confluent.sigmarules.streams.StreamManager;

public class SigmaProcessorManager {
    private StreamManager streamManager;

    public SigmaProcessorManager(StreamManager streamManager) {
        this.streamManager = streamManager;
    }

    public SourceProcessor getSourceProcessor() {
        // create the source processor
        if (streamManager.getInputFormat() == TopicFormatEnum.AVRO) {
            return new AvroSourceProcessor();
        } else {
            return new JsonSourceProcessor();
        }
    }

    public SinkProcessor getSinkProcessor() {
        // create the sink processor
        if (streamManager.getOutputFormat() == TopicFormatEnum.AVRO) {
            return new AvroSinkProcessor(streamManager.getStreamProperties().getProperty("schema.registry"));
        } else {
            return new JsonSinkProcessor();
        }
    }
}
