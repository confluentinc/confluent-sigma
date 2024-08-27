package io.confluent.sigmarules.processor.json;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.sigmarules.models.DetectionResults;
import io.confluent.sigmarules.processor.SinkProcessor;

public class JsonSinkProcessor  implements SinkProcessor {

    @Override
    public void addSinkProcessor(KStream<String, DetectionResults> sigmaStream) {
        sigmaStream.to(detectionTopicNameExtractor, 
                Produced.with(Serdes.String(), getJsonSerde()));
    }

    private final TopicNameExtractor<String, DetectionResults> detectionTopicNameExtractor =
        (key, results, recordContext) -> results.getOutputTopic();


    public Serde<DetectionResults> getJsonSerde(){
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", DetectionResults.class);
        final Serializer<DetectionResults> detectionSer = new KafkaJsonSerializer<>();
        detectionSer.configure(serdeProps, false);

        final Deserializer<DetectionResults> detectionDes = new KafkaJsonDeserializer<>();
        detectionDes.configure(serdeProps, false);
        return Serdes.serdeFrom(detectionSer, detectionDes);
    }

}
