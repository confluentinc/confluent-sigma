#!/bin/bash

# These scripts are intended to be run from the test directory
#
# This script runs kafka-consumer-perf-test with 4 parallel consumers
# to measure Kafka consumer performance

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

# Source auto-configure script to get configuration variables
if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi

# Check if sigma properties file exists
if [ ! -f "$SIGMA_PROPS" ] ; then
  echo "sigma properties not found. Suggested path from auto-configure is $SIGMA_PROPS"
  exit 1
fi

# Check if bootstrap servers are configured
if [ -z "$BOOTSTRAP_SERVERS" ]; then
  echo "BOOTSTRAP_SERVERS not configured. Check your properties file ($SIGMA_PROPS)."
  exit 1
fi

# Create output directory
mkdir -p perf_test_results
cd perf_test_results

# Generate unique timestamp-based identifier for this test run
TEST_RUN_ID=$(date +%Y%m%d_%H%M%S)_${RANDOM}

# Base command using auto-configured variables
BASE_CMD="kafka-consumer-perf-test --messages 25000000 --bootstrap-server ${BOOTSTRAP_SERVERS} --timeout 1000000 --consumer.config ${SIGMA_PROPS} --topic dns50"

# Start timestamp
START_TIME=$(date)
echo "Starting performance test at: $START_TIME"
echo "Test run ID: $TEST_RUN_ID"
echo "Using bootstrap servers: ${BOOTSTRAP_SERVERS}"
echo "Using properties file: ${SIGMA_PROPS}"

# Run 4 instances in parallel with unique consumer groups
for i in {1..4}; do
    GROUP_NAME="perf_${TEST_RUN_ID}_${i}"
    OUTPUT_FILE="perf_test_${i}.out"
    
    echo "Starting consumer $i with group: $GROUP_NAME"
    nohup $BASE_CMD --group $GROUP_NAME > $OUTPUT_FILE 2>&1 &
    
    # Store the PID
    echo $! > "perf_test_${i}.pid"
done

echo "All 4 consumers started. PIDs:"
cat perf_test_*.pid

echo "Waiting for all processes to complete..."

# Wait for all background jobs to finish
wait

END_TIME=$(date)
echo "All tests completed at: $END_TIME"

# Show results summary
echo "=== PERFORMANCE TEST RESULTS ==="
echo "Test run ID: $TEST_RUN_ID"
echo "Start time: $START_TIME"
echo "End time: $END_TIME"
echo ""


# Parse and display results from each test
total_messages=0
total_mb=0.0
combined_throughput_msgs=0.0
combined_throughput_mb=0.0

for i in {1..4}; do
    echo "=== Consumer $i Results ==="
    if [ -f "perf_test_${i}.out" ]; then
        # Extract the data line (second line) - skip header
        DATA_LINE=$(sed -n '2p' perf_test_${i}.out)
        if [ ! -z "$DATA_LINE" ]; then
            echo "Raw data: $DATA_LINE"
            
            # Parse CSV format: start.time, end.time, data.consumed.in.MB, MB.sec, data.consumed.in.nMsg, nMsg.sec, ...
            START_TIME=$(echo "$DATA_LINE" | cut -d',' -f1 | xargs)
            END_TIME=$(echo "$DATA_LINE" | cut -d',' -f2 | xargs)
            MB_CONSUMED=$(echo "$DATA_LINE" | cut -d',' -f3 | xargs)
            MB_SEC=$(echo "$DATA_LINE" | cut -d',' -f4 | xargs)
            MESSAGES_CONSUMED=$(echo "$DATA_LINE" | cut -d',' -f5 | xargs)
            MSG_SEC=$(echo "$DATA_LINE" | cut -d',' -f6 | xargs)
            
            echo "Start Time: $START_TIME"
            echo "End Time: $END_TIME"
            echo "Messages Consumed: $MESSAGES_CONSUMED"
            echo "MB Consumed: $MB_CONSUMED"
            echo "Messages/sec: $MSG_SEC"
            echo "MB/sec: $MB_SEC"
            
            # Add to totals using awk for float arithmetic
            total_messages=$((total_messages + MESSAGES_CONSUMED))
            total_mb=$(echo "$total_mb + $MB_CONSUMED" | awk '{print $1+$3}')
            combined_throughput_msgs=$(echo "$combined_throughput_msgs + $MSG_SEC" | awk '{print $1+$3}')
            combined_throughput_mb=$(echo "$combined_throughput_mb + $MB_SEC" | awk '{print $1+$3}')
        else
            echo "No data found in output file"
            echo "--- Full output ---"
            cat perf_test_${i}.out
            echo "--- End output ---"
        fi
    else
        echo "Output file not found"
    fi
    echo ""
done

echo "=== COMBINED TOTALS ==="
echo "Total Messages Consumed: $total_messages"
printf "Total MB Consumed: %.2f\n" $total_mb
printf "Combined Throughput (messages/sec): %.2f\n" $combined_throughput_msgs
printf "Combined Throughput (MB/sec): %.2f\n" $combined_throughput_mb


