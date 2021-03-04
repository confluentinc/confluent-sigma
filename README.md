### start FS Connector
export CLASSPATH="$(find target/ -type f -name '*.jar'| grep '\-package' | tr '\n' ':')"
connect-standalone /usr/local/bin/confluent/etc/kafka/connect-standalone.properties config/kafka-connect-fs.properties

### cleanup
kafka-topics --bootstrap-server localhost:9092 --topic sigma_rules_cache --delete
kafka-topics --bootstrap-server localhost:9092 --topic sigma_rules_raw --delete
rm /tmp/connect.offsets

### kcache topic creation
kafka-topics --bootstrap-server localhost:9092 --topic sigma_rules --replication-factor 1 --partitions 1 --config cleanup.policy=compact --create

# File System Connector topic
kafka-topics --bootstrap-server localhost:9092 --topic sigma_rules_fs --replication-factor 1 --partitions 1 --create


### sigma rules consumer
kafka-console-consumer --bootstrap-server localhost:9092 --topic sigma_rules --from-beginning


### Produce Sample Data
kafka-console-producer --bootstrap-server localhost:9092 --topic sigma_rules --property "parse.key=true" --prorty "key.separator=:"

{"title":Sigma Rule Test,"id":"123456789","status":"experimental","description":"This is just a test.","author":"Test","date":"1970/01/01","references":["https://confluent.io/"],"tags":["test.test"],"logsource":{"category":"process_creation","product":"windows"},"detection":{"selection":{"CommandLine|contains|all":[" /vss "," /y "]},"condition":"selection"},"fields":["CommandLine","ParentCommandLine"],"falsepositives":["Administrative activity"],"level":"high"}

### Regex Patterns
https://uncoder.io/ - generates grep regex patterns (very basic)
https://www.regexplanet.com/advanced/java/index.html - generates java strings from grep regex patterns