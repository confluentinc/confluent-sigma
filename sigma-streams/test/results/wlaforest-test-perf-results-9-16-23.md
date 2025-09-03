# Performance Testing 

Testing was performed using Confluent Cloud basic cluster in AWS us-east-1.  zeek dns data was loaded into two different 
topics one with 10 partitions (confluent-benchmark-2) and one with 50 paritions (confluent-benchmark-1).  Streams app ran 
on a single m4.2xlarge (8 vcpu 32 GB) box with no other processes running in AWS us-east-1.  Data collection through CC 
Cloud Metrics API (1 minute samples extrapolated to per second)

## Test setup

Each benchmark test is set up using [test/bin/test-setup.sh x](../bin/test-setup.sh) where x is the testcase.  This 
script copies a sigma.properties files to ~/.confluent/benchmark.properties (or whatever directory it finds in 
auto-configurations) and then appends that with config information (like authentication) from the existing 
sigma.properties in that directory. It also loads in all the corresponding sigma rules associated with the test.

Confluent Cloud metrics are queries and summarized by the [check-process-rate.py](../bin/check-process-rate.py)

## Release testing

This is the results for release 1.3.0.  Previous test results can be found [here](./).  It should be noted that the
input data chanegd between this benchmark and previous ones.  The average size of records went down by about 30% due to 
the removal of the one record with a very long query string.  The upshot of this is that while records processed per
second remained about the same the bytes per second went down.  CPU remains close to saturation on all tests and a 
future benchmark should show throughput when running confluent-sigma over multiple machines.

## Baseline perf test

Using the command 

`kafka-consumer-perf-test --messages 20000000 --consumer.config /mnt/config/sigma.properties --topic test2 \ 
  --threads 1 --group CONSUMER-PERF-XXXX`

- kafka-consumer-perf-test, 5 threads: 
   - 586 MB/s, 574 MB/s 
   - 1.2M, 1.2M messages/second
    
## Test Case 1 (Simple 50 partitions)

- [rules](../benchmarks/rules/1)
- [config](../benchmarks/configs/1/benchmark.properties)

### Params

- No tuning
- Date - 9/16/23
- Data - 9 unique records (test/data/test-zeek-dns.txt)
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Output - 50 Partitions - Uncompressed
  - Sigma Rules - 1 rule Single field exact match (one in 6 records match)
    - test/rules/single-field-match.yaml
- App - 50 threads, 1 machine

### Results

- Average bytes per second 16,679,009.05
- Max bytes per second 16,826,161.52
- Min bytes per second 16,368,011.65
- Bytes Standard deviation 8,695,252.30
- Bytes range delta 458,149.87

- Average records per second 447,867.86
- Max records per second 449,962.18
- Min records per second 444,872.97
- Records Standard deviation 93,042.65
- Records range delta 5,089.22

- %Cpu 93.5, 0.0 wa, App 795% CPU

Note that when we split this test across 2 seperate ec2 instances each with 25 threads records
per second was 881k. 

Spreading the load across 3 ec2 instances each with 17 threads each we saw 1.3M 
records per second.  Also note that with three ec2 instances total machine CPU utilization was observed to be 
hovering around 93% .


## Test Case 2 (Simple 10 partitions)

- [rules](../benchmarks/rules/2)
- [config](../benchmarks/configs/2/benchmark.properties)

### Params

- no tuning
- Date - 9/17/23
- Data - 9 unique records
- Source - 10 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/2/single-field-match.yaml
- App - 10 threads, 1 machine

### Results

- Average bytes per second 16,667,184.87
- Max bytes per second 17,960,593.17
- Min bytes per second 16,333,875.35
- Bytes Standard deviation 34,750,928.99
- Bytes range delta 1,626,717.82

- Average records per second 440,591.99
- Max records per second 445,189.40
- Min records per second 438,167.95
- Records Standard deviation 161,871.09
- Records range delta 7,021.45

## Test Case 3 (REGEX 50 partitions)

- [rules](../benchmarks/rules/3)
- [config](../benchmarks/configs/3/benchmark.properties)

### Params

- No tuning
- Date - 9/18/23
- Data - 9 unique records
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 1 rule regex with long field `^.{50}.*$`
- 1 Apps - 50 threads, 1 machine

### Results

- Average bytes per second 15,542,244.36
- Max bytes per second 15,755,537.87
- Min bytes per second 15,243,612.75
- Bytes Standard deviation 8,908,045.64
- Bytes range delta 511,925.12

- Average records per second 414,697.98
- Max records per second 419,599.18
- Min records per second 405,910.68
- Records Standard deviation 258,010.36
- Records range delta 13,688.50

## Test Case 4 (100 simple rules 50 partitions)

- [rules](../benchmarks/rules/4)
- [config](../benchmarks/configs/4/benchmark.properties)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 100 rules all single field checks.  One matching the rest not matching
- 1 Apps - 50 threads, 1 machine

### Results

- Average bytes per second 1,559,600.32
- Max bytes per second 1,630,970.15
- Min bytes per second 1,516,010.43
- Bytes Standard deviation 2,448,238.47
- Bytes range delta 114,959.72

- Average records per second 42,009.09
- Max records per second 42,899.07
- Min records per second 41,046.08
- Records Standard deviation 42,194.33
- Records range delta 1,852.98

- ConfluentSigma runner system CPU load CPU 98.5%, 794% Java