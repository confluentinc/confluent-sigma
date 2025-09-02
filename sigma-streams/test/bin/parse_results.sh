#!/bin/bash

cd perf_test_results

echo "=== KAFKA CONSUMER PERFORMANCE TEST RESULTS ==="
echo ""

# Parse and display results from each test
total_messages=0
total_mb=0.0
combined_throughput_msgs=0.0
combined_throughput_mb=0.0

for i in {1..4}; do
    echo "=== Consumer $i Results ==="
    if [ -f "perf_test_${i}.out" ]; then
        # Extract the data line (second line)
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
            total_messages=$(($total_messages + $MESSAGES_CONSUMED))
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

# Calculate test duration
if [ -f "perf_test_1.out" ]; then
    FIRST_START=$(sed -n '2p' perf_test_1.out | cut -d',' -f1 | xargs)
    LAST_END=$(sed -n '2p' perf_test_1.out | cut -d',' -f2 | xargs)
    echo ""
    echo "Test Duration: From $FIRST_START to $LAST_END"
fi

