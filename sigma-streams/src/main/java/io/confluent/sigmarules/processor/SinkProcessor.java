package io.confluent.sigmarules.processor;

import org.apache.kafka.streams.kstream.KStream;

import io.confluent.sigmarules.models.DetectionResults;

public interface SinkProcessor {
    public void addSinkProcessor(KStream<String, DetectionResults> sigmaStream);

}
