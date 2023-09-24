# Relaese notes for 1.3.0

## Rule based output topics

Prior to this feature all records that matched any rule for a configured sigma stream processor would be sent to the 
same output topic.  With this feature we extended the SIGMA specification and added syntax to the sigma rule that allows 
you to specify the output topic for any records that match the specific rule.  If no output topic is provided then the
output topic configured for the stream processor will be used.  And example rules that includes this is 
[regex_rule.yml](sigma-streams/config/rules/regex_rule.yml)

All new extensions specific to kafka, including this rule output topic, will be in a kafka section.  An example of the 
section would be

```
kafka:
  outputTopic: firewalls
```

## Regex value capture and emission

While previous versions of SigmaStreams correctly match regex patterns `RE|` in sigma rules this version enables one
to take matching values and then extract them into an output field.  For the following field

```
title: Regex Test Rule
description: This rule is the regex rule test
author: Mike Peacock
logsource:
  product: splunk
  service: cisco:asa
detection:
  filter_field:
    sourcetype: cisco:asa
  event_match:
    event|re: '^(?<timestamp>\w{3}\s\d{2}\s\d{2}:\d{2}:\d{2})\s(?<hostname>[^\s]+)\s\%ASA-\d-(?<messageID>[^:]+):\s(?<action>[^\s]+)\s(?<protocol>[^\s]+)\ssrc\sinside:(?<src>[0-9\.]+)\/(?<srcport>[0-9]+)\sdst\soutside:(?<dest>[0-9\.]+)\/(?<destport>[0-9]+)'
  condition: filter_field AND event_match
kafka:
  outputTopic: firewalls
  customFields:
    location: edge
    sourcetype: cisco:asa
    index: main
```

timestamp, hostname, messageID, etc will be matched by the regular expression and that matching values will be extracted
into those as new output fields in the resulting record.  This is extremely useful if you want to use sigma rules to
extract structured output from unstructured or semi-structured input.

## Rule based static field addition

In the kafka section you now have the ability to provide list of custom fields and static values to insert for those 
fields.  These will be added in the root of the matching output records.  This can enable metadata for use by down 
stream processors or event tools like a SOAR. With this the rules can not only service to find detections but to provide
directions on what to do with them.

```
kafka:
  customFields:
    location: edge
    sourcetype: cisco:asa
    index: main
```

## SigmaStream registration and tracking

Running SigmaStream applications now register themselves in Kafka along with metadata about its state.  This information
is viewable from the SigmaStreams UI.  This data goes into the topic `sigma-app-instances`.  A sample of this data can 
seen here:

```
{
  "applicationId": "confluent-streams-benchmark-4-20230621031743",
  "kafkaStreamsState": "NOT_RUNNING",
  "numRules": 100,
  "sampleTimestamp": 1687319749050,
  "sampleTimestampHr": "2023-06-21 03:55:49",
  "threadMetadata": [],
  "appHostName": "xxxxx.ec2.internal",
  "appProperties": {
    "commit.interval.ms": 1000,
    "bootstrap.servers": "xxx.us-east-1.aws.confluent.cloud:9092",
    "data.topic": "confluent-benchmark-1",
    "output.topic": "dns-detection",
    "sigma.rules.topic": "sigma-rules",
    "default.value.serde": "org.apache.kafka.common.serialization.Serdes$StringSerde",
    "sigma.rule.filter.service": "dns",
    "field.mapping.file": "config/zeek-mapping.yml",
    "default.key.serde": "org.apache.kafka.common.serialization.Serdes$StringSerde",
    "sigma.rule.filter.product": "zeek",
    "num.stream.threads": "50",
    "auto.offset.reset": "earliest",
    "application.id": "confluent-streams-benchmark-4-20230621031743"
  },
  "applicationInstanceId": "confluent-streams-benchmark-4-20230621031743b4cf5a26-4e0a-4d95-b1a3-3c51e4a6e735",
  "key": "confluent-streams-benchmark-4-20230621031743b4cf5a26-4e0a-4d95-b1a3-3c51e4a6e735"
}
```

## Stop after first match

In some scenarios you may potentially have thousands of sigma rules used to route data.  In this case you may not care
how many runes or which ones match but instead that any of them match.  To support this we have added the ability to 
stop matching rules after the first matching sigma rule.  To turn this on use `sigma.rule.first.match=true` in the
configuration

## Confluent Sigma UI redone

The user interface provided by the sigma-streams-ui module has been completely re-done.  It should still be considered
a tool for developing and testing but its is great improved from the previous demo oriented one.  it would take forever 
to go through the improvements so its better to just see it yourself.  Nuff said!

## Remove null values

In earlier versions serialization of matches or sigma rules that had no value for an existing field would assign
a string value of `null`.  In this version there are no `null` fields but they are instead absent from the serialized 
output.

## Matched metric

SigmaStreams now tracks how many records has matched its rules.
