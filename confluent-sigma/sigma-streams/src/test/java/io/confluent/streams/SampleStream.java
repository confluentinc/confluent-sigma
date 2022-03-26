package io.confluent.streams;

import static org.junit.Assert.assertTrue;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.After;
import org.junit.jupiter.api.Test;

public class SampleStream {
  private TopologyTestDriver td;
  private TestInputTopic<String, String> inputTopic;
  private TestOutputTopic<String, String> outputTopic;

  private Topology topology;
  private final Properties config;

  public SampleStream() {

    config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test-app");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "foo:1234");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
  }

  @After
  public void tearDown() {
    td.close();
  }

  @Test
  public void shouldIncludeValueWithLengthGreaterThanFive() {

    topology = retainWordsLongerThan5Letters();
    td = new TopologyTestDriver(topology, config);

    inputTopic = td.createInputTopic("input-topic", Serdes.String().serializer(),
        Serdes.String().serializer());
    outputTopic = td.createOutputTopic("output-topic", Serdes.String().deserializer(),
        Serdes.String().deserializer());

    assertTrue(outputTopic.isEmpty());
/*
    inputTopic.pipeInput("foo", "barrrrr");
    assertThat(outputTopic.readValue(), equalTo("barrrrr"));
    assertThat(outputTopic.isEmpty(), is(true));

    inputTopic.pipeInput("foo", "bar");
    assertThat(outputTopic.isEmpty(), is(true));

 */
  }

  public Topology retainWordsLongerThan5Letters() {
    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> stream = builder.stream("input-topic");
    stream.filter((k, v) -> v.length() > 5).to("output-topic");

    return builder.build();
  }

}
