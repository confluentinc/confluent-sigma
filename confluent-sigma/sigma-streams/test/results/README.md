# Performance Testing 

Testing was performed using Confluent Cloud basic cluster in AWS us-east-1.  zeek dns data was loaded into a single 
topic.  Streams app ran on a single m4.2xlarge (8 vcpu 32 GB) box with no other processes running in AWS 
us-east-1.  Data collection through CC Cloud Metrics API (1 minute samples extrapolated to per second)

## Test setup

Each benchmark test is setup using  `test/bin/test-setup.sh x` where x is the testcase.  This script copys a 
sigma.properties files to ~/.confluent/benchmark.properties and then appends that with config information 
(like authentication) from the exsiting sigma.properties in that directory.  It also loads in all the 
corresponding sigma rules associated with the test.

Confluent Cloud metrics are queries and summarized by the `check-process-rate.py` script.

## Baseline perf test

Using the command 

`kafka-consumer-perf-test --messages 20000000 --consumer.config /mnt/config/sigma.properties --topic test2 \ 
  --threads 1 --group CONSUMER-PERF-XXXX`

- kafka-consumer-perf-test, 5 threads: avg 485.50 MB/sm  1.03M messages/second

## Test Case 1 (Simple 50 partitions)

### Params

- No tuning
- Date - 6/5/23
- Data - 9 unique records (test/data/test-zeek-dns.txt)
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Output - 50 Partitions - Uncompressed
  - Sigma Rules - 1 rule Single field exact match (one in 6 records match)
    - test/rules/single-field-match.yaml
- App - 50 threads, 1 machine

### Results

- Average bytes per second 24,105,111.88
- Max bytes per second 24,404,906.62
- Min bytes per second 23,727,671.58
- Bytes Standard deviation 17,147,237.38
- Bytes range delta 677,235.03

- Average records per second 447,780.14
- Max records per second 451,125.45
- Min records per second 444,720.77
- Records Standard deviation 108,820.69
- Records range delta 6,404.68

- %Cpu 93.5, 0.0 wa, App 795% CPU

## Test Case 2 (Simple 10 partitions)

### Params

- no tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 10 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/single-field-match.yaml
- App - 10 threads, 1 machine

### Results

- Average bytes per second 17,272,838.71
- Max bytes per second 17,401,580.22
- Min bytes per second 17,138,538.12
- Bytes Standard deviation 5,085,621.45
- Bytes range delta 263,042.10

- Average records per second 462,781.25
- Max records per second 466,462.82
- Min records per second 459,465.43
- Records Standard deviation 150,218.82
- Records range delta 6,997.38

- %Cpu 87.5, 0.0 wa, App 737.1% CPU

## Test Case 3 (REGEX 50 partitions)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 1 rule regex with long field `^.{50}.*$`
- 1 Apps - 50 threads, 1 machine

### Results
- Average bytes per second 15,836,954.15
- Max bytes per second 15,960,337.52
- Min bytes per second 15,519,961.07
- Bytes Standard deviation 8,502,646.51
- Bytes range delta 440,376.45

- Average records per second 423,618.42
  - Max records per second 426,124.30
- Min records per second 417,141.63
- Records Standard deviation 173,630.56
- Records range delta 8,982.67

## Test Case 4 (100 simple rules 50 partitions)

### Params

- No tuning
- Date - 4/12/22
- Data - 9 unique records
- Source - 50 Partitions, 1,133,268,321 records - Snappy
- Sigma Rules - 100 rules all single field checks.  One matching the rest not matching
- 1 Apps - 50 threads, 1 machine

### Results

- Average bytes per second 1,623,992.30
- Max bytes per second 1,685,943.75
- Min bytes per second 1,570,249.62
- Bytes Standard deviation 2,548,356.80
- Bytes range delta 115,694.13

- Average records per second 43,494.55
- Max records per second 45,616.05
- Min records per second 42,092.02
- Records Standard deviation 68,383.45
- Records range delta 3,524.03

- CPU 98.5%, 797