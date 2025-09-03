/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package io.confluent.sigmarules.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.sigmarules.config.SigmaPropertyEnum;
import io.confluent.sigmarules.config.TopicFormatEnum;

public class StreamManager {
    final static Logger logger = LogManager.getLogger(StreamManager.class.getName());

    protected Properties properties = new Properties();
    private AdminClient client = null;
    private Integer recordsProcessed = 0;
    private Integer numMatches = 0;
    protected String inputTopic;
    protected String outputTopic;
    protected TopicFormatEnum inputFormat = TopicFormatEnum.JSON;
    protected TopicFormatEnum outputFormat = TopicFormatEnum.JSON;
    protected Boolean firstMatch;

    public StreamManager(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("null properties not allowed");
        }

        this.properties.putAll(properties);
        this.properties.put(StreamsConfig.APPLICATION_ID_CONFIG, properties.getProperty("application.id"));
        this.properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
        this.properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        this.properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        this.properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        this.firstMatch = Boolean.valueOf(
            properties.getProperty(SigmaPropertyEnum.SIGMA_RULE_FIRST_MATCH.toString()));

        setTopicProperties();

        try {
            client = AdminClient.create(this.properties);
        } catch (KafkaException e) {
            logger.error(e);
        }

    }

    public String getInputTopic() {
        return inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public TopicFormatEnum getInputFormat() {
        return inputFormat;
    }

    public TopicFormatEnum getOutputFormat() {
        return outputFormat;
    }

    public Boolean getFirstMatch() {
        return firstMatch;
    }

    private void setTopicProperties() {
        this.outputTopic = properties.getProperty(SigmaPropertyEnum.OUTPUT_TOPIC.toString());
        this.inputTopic = properties.getProperty(SigmaPropertyEnum.DATA_TOPIC.toString());

        if (null != properties.getProperty("data.topic.format") &&
            properties.getProperty("data.topic.format").toUpperCase().matches("AVRO")) {
            this.inputFormat = TopicFormatEnum.AVRO;
        }

        if (null != properties.getProperty("output.topic.format") &&
            properties.getProperty("output.topic.format").toUpperCase().matches("AVRO")) {
            this.outputFormat = TopicFormatEnum.AVRO;
        }

        if ((inputFormat == TopicFormatEnum.AVRO) || (outputFormat == TopicFormatEnum.AVRO)) {
            // TODO: throw exception if schema.registry.url is not set    
            this.properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty("schema.registry"));
        }

    }

    public String getApplicationId() {
        return this.properties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
    }

    public Properties getStreamProperties() {
        return this.properties;
    }

    public void createTopic(String topicName) {
        if (client != null) {
            List<NewTopic> topics = new ArrayList<>();
            topics.add(new NewTopic(topicName, Optional.empty(),
                Optional.empty()));

            logger.info("creating topics " + Arrays.toString(topics.toArray()));
            CreateTopicsResult result = client.createTopics(topics);
            KafkaFuture<Void> future = result.values().get(topicName);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            try {
                if (future != null)
                    future.get();
            } catch (InterruptedException e) {
                logger.error("Topic creation was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // Check if the topic already exists
                if (e.getCause() != null && e.getCause().getClass().getSimpleName().contains("TopicExists")) {
                    logger.info("Topic '{}' already exists, skipping creation", topicName);
                } else {
                    logger.error("Failed to create topic '{}': {}", topicName, e.getMessage());
                }
            }
        } else {
            logger.error("No admin client initialized");
        }
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getNumMatches() {
        return numMatches;
    }

    public void setNumMatches(Integer numMatches) {
        this.numMatches = numMatches;
    }
}
