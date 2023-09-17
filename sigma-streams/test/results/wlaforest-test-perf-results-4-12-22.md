# Performance Testing 

Testing was performed using Confluent Cloud basic cluster in AWS us-east-1.  zeek dns data was loaded into a single 
topic.  Streams app ran on a single m4.2xlarge (8 vcpu 32 GB) box with no other processes running in AWS 
us-east-1.  Data collection through CC Cloud UI Metrics (1 minute samples extrapolated to per second)

Baseline kafka-consumer-perf-test results 

- kafa-cosumer-perf-test 2.00M RPS Avg with 3 process
- kafa-cosumer-perf-test 2.17M RPS Avg with 5 processes close to saturation

## Test Case 1 (Simple 50 partitions)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records (test/data/test-zeek-dns.txt)
- Source - 50 Partitions, 213,256,533 records - Snappy
- Output - 50 Partitions - Uncompressed
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/single-field-match.yaml
- App - 50 threads, 1 machine

### Results

- **Consumption: 412k RPS avg**
- Production: 46k RPS Avg
- %Cpu 94.1, 0.0 wa, App 792% CPU

## Test Case 2 (Simple 10 partitions)

### Params

- no tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 10 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/single-field-match.yaml
- App - 10 threads, 1 machine

### Results

- **Consumption: 447K RPS avg**
- Production: 49.2k RPS avg

## Test Case 3 (REGEX 50 partitions)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 50 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 1 rule regex with long field `^.{50}.*$`
  - test/rules/2
- 1 Apps - 50 threads, 1 machine

### Results
- **Consumption: 401.7k RPS avg**
- Production: 44k RPS avg
- %Cpu 92.9, 0.0 wa, Apps - 787% CPU

## Test Case 4 (100 simple rules 50 partitions)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 50 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 100 rules all single field checks.  One matching the rest not matching
  - test/rules/3
- 1 Apps - 50 threads, 1 machine

### Results

- **Consumption: 42.8 RPS Avg**
  - %Cpu 95.8, 0.0 wa, Apps - 781.4% CPU
