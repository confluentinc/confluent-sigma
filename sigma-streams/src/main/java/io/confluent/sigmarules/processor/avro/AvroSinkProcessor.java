package io.confluent.sigmarules.processor.avro;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.models.DetectionResultsAvro;
import io.confluent.sigmarules.processor.SinkProcessor;

public class AvroSinkProcessor implements SinkProcessor {
    private String schemaRegistryUrl;

    public AvroSinkProcessor(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public void addSinkProcessor(KStream<String, DetectionResults> sigmaStream) {
        sigmaStream
            .mapValues(detectionResults -> jsonToAvro(detectionResults))
            .to(detectionTopicNameExtractor);
    }


    private final TopicNameExtractor<String, DetectionResultsAvro> detectionTopicNameExtractor =
        (key, results, recordContext) -> results.getOutputTopic();

    /* 
    public Serde<DetectionResults> getAvroSerde() {
        final Map<String, String> serdeConfig = 
            Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
        
        final Serde<DetectionResults> detectionResultsAvroSerde = null; //new SpecificAvroSerde<>();
        detectionResultsAvroSerde.configure(serdeConfig, false); // `false` for record values

        return detectionResultsAvroSerde;

    }
    */
     
    public DetectionResultsAvro jsonToAvro(DetectionResults detections) {
        DetectionResultsAvro detectionResultsAvro = new DetectionResultsAvro();
        detectionResultsAvro.setTimeStamp(detections.getTimeStamp());
        detectionResultsAvro.setTitle(detections.getTitle());
        detectionResultsAvro.setId(detections.getId());
        detectionResultsAvro.setOutputTopic(detections.getOutputTopic());

        if (!detections.getCustomFields().isEmpty()) {
            detectionResultsAvro.setCustomFields(detections.getCustomFields());
        }

        detectionResultsAvro.setSourceData(detections.getSourceData());

        return detectionResultsAvro;        
    }

}
