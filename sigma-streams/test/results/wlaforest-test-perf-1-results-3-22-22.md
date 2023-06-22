# Performance Testing 

Testing was performed using Confluent Cloud basic cluster in AWS us-east-1.  zeek dns data was loaded into a single 
topic.  Streams app ran on a single m4.2xlarge (8 vcpu 32 GB) box with no other processes running in AWS 
us-east-1.  Data collection through CC Cloud UI Metrics (1 minute samples extrapolated to per second)

## Test Case 1

### Params

- No tuning
- Date - 4/1/22
- Data - 9 unique records (test/data/test-zeek-dns.txt)
- Source - 50 Partitions, Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/single-field-match.yaml
- App - 50 threads, 1 machine

### Results

- kafa-cosumer-perf-test 2.00M RPS Avg with 3 process
- kafa-cosumer-perf-test 2.17M RPS Avg with 5 processes close to saturation
- Production: 45.4k RPS Avg
- **Consumption: 406k RPS avg**
- %Cpu 93.4, 0.0 wa, App 792% CPU

## Test Case 2

### Params

- no tuning
- Date - 3/22/22
- Data - 6 unique records
- Source - 10 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - - test/rules/single-field-match.yaml
- App - 10 threads, 1 machine

### Results

- ~ 13 minute sustained processing
- Production: 17.6k RPS Peek, 17.3k RPS Avg
- **Consumption: 158k RPS Peek, 155k RPS Avg**
- %Cpu 45.5, 0.0 wa, App 440% CPU
- %Cpu 48.8, 0.0 wa, App 429% CPU

## Test Case 3

### Params

- No tuning
- Date - 3/22/22
- Data - 6 unique records
- Source - 50 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 1 rule Single field exact match (one in 6 records match)
  - test/rules/1
- 2 Apps - 25 threads, 1 machine

### Results

- ~ 13 minute sustained processing
- kafa-cosumer-perf-test 2.00M RPS Avg with 3 process
- kafa-cosumer-perf-test 2.17M RPS Avg with 5 processes close to saturation
- **Consumption: 224k RPS Peek, 223k RPS Avg**
- %Cpu 88.2, 0.0 wa, Apps - 379%, 367% CPU
- %Cpu 88.4, 0.0 wa, Apps - 378%, 371% CPU

## Test Case 4

### Params

- No tuning
- Date - 3/23/22
- Data - 6 unique records
- Source - 50 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 1 rule regex with long field `^.{50}.*$`
  - test/rules/2
- 1 Apps - 50 threads, 1 machine

### Results

- ~ 13 minute sustained processing
- kafa-cosumer-perf-test 2.00M RPS Avg with 3 process
- kafa-cosumer-perf-test 2.17M RPS Avg with 5 processes close to saturation
- **Consumption: 143k RPS Peek, 136k RPS Avg**
- %Cpu 48.4, 0.0 wa, Apps - 427% CPU

## Test Case 5

### Params

- No tuning
- Date - 3/23/22
- Data - 6 unique records
- Source - 50 Partitions, 213,256,533 records - Snappy
- Sigma Rules - 100 rules all single field checks.  One matching the rest not matching
  - test/rules/3
- 1 Apps - 50 threads, 1 machine

### Results

- ~ 13 minute sustained processing
- kafa-cosumer-perf-test 2.00M RPS Avg with 3 process
- kafa-cosumer-perf-test 2.17M RPS Avg with 5 processes close to saturation
- **Consumption: 114k RPS Peek, 109k RPS Avg**
  - %Cpu 56.2, 0.0 wa, Apps - 486% CPU
