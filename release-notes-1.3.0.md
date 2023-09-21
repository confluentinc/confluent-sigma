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
  customFields:
    location: edge
    sourcetype: cisco:asa
    index: main

```

