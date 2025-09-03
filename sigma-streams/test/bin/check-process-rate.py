#!/usr/bin/python3

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script is used to check the process rate of the Confluent Sigma application.  It expects as arguments 
# 1 the path to an confluent cloud API Key text file. 
# 2 the resource ID for the Kafka cluster like lkc-1234567890
# 3 the topic name for the Kafka topic to check the process rate for

# USAGE:
# python3 check-process-rate.py <confluent cloud API Key text file> <resource ID> <topic name>

import json
import requests
import pytz
import statistics
import argparse
import base64

from datetime import datetime, timedelta

import os

def calculate_values(json_metrics):

    if 'data' not in json_metrics or not json_metrics['data']:
        values = []
    else:
        values = [item['value'] for item in json_metrics['data']]

    if values is None or len(values) == 0:
        print("Error: Metrics API returned no data points. Most likely cause is that Confluent Sigma is not running.")
        exit()
    elif len(values) < 3:
        print("Error: Not enough data points returned (less than three). Please wait for more data to accumulate.")
        exit()

    # Calculate the maximum, minimum, and average per second
    max_value = max(values[1:-1])/60
    min_value = min(values[1:-1])/60
    avg_value = sum(values[1:-1]) / len(values[1:-1])/60
    std_deviation = statistics.stdev(values[1:-1])
    range = max_value - min_value
    
    return {'maximum': max_value, 'minimum': min_value, 'average': avg_value,
            'std_deviation': std_deviation, 'range': range}

def build_human_summary(bytes_result, records_results):
    human_summary = \
        "Average bytes per second " + format(bytes_result["average"], ",.2f") + '\n' +\
        "Max bytes per second " + format(bytes_result["maximum"], ",.2f") + '\n' +\
        "Min bytes per second " + format(bytes_result["minimum"], ",.2f") + '\n' +\
        "Bytes Standard deviation " + format(bytes_result["std_deviation"], ",.2f") + '\n' + \
        "Bytes range delta " + format(bytes_result["range"], ",.2f") + '\n\n' + \
        "Average records per second " + format(records_results["average"], ",.2f") + '\n' + \
        "Max records per second " + format(records_results["maximum"], ",.2f") + '\n' + \
        "Min records per second " + format(records_results["minimum"], ",.2f") + '\n' + \
        "Records Standard deviation " + format(records_results["std_deviation"], ",.2f") + '\n' + \
        "Records range delta " + format(records_results["range"], ",.2f")
    return human_summary

def get_auth_token_from_file(api_key_file):
        """
        Reads the API key and secret from the given file and returns a base64-encoded auth token.
        Expects the file to be in the format:
        === Confluent Cloud API key ===
        API key:
        <your-api-key>
        API secret:
        <your-api-secret>
        """
        with open(api_key_file, 'r') as f:
            content = f.read()

        key = None
        secret = None

        lines = content.split('\n')
        for i, line in enumerate(lines):
            line = line.strip()
            if line == "API key:" and i + 1 < len(lines):
                key = lines[i + 1].strip()
            elif line == "API secret:" and i + 1 < len(lines):
                secret = lines[i + 1].strip()

        if not key or not secret:
            print("Error: Could not find API key and secret in the expected format.")
            print("Expected format:")
            print("=== Confluent Cloud API key ===")
            print("API key:")
            print("<your-api-key>")
            print("API secret:")
            print("<your-api-secret>")
            exit(1)

        key_secret = f"{key}:{secret}"
        auth_token = base64.b64encode(key_secret.encode('utf-8')).decode('utf-8')
        return auth_token

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="retrieve bytes and record send data")

    # Required parameters
    parser.add_argument("apiKeyFile", help="path to the confluent cloud API Key text file")
    parser.add_argument("resourceId", help="resource for the confluent cluster")
    parser.add_argument("topic", help="topic you want a record count for")

    args = parser.parse_args()
    # Check that the apiKeyFile exists
    if not os.path.isfile(args.apiKeyFile):
        print(f"Error: API key file '{args.apiKeyFile}' does not exist.")
        exit(1)


    auth_token = get_auth_token_from_file(args.apiKeyFile)
    
    current_datetime = datetime.now(pytz.timezone("America/New_York"))
    offset = current_datetime.strftime("%z")
    offset_formatted = f"{offset[:-2]}:{offset[-2:]}"

    end_time = current_datetime.strftime("%Y-%m-%dT%H:%M:%S") + offset_formatted

    # Calculate the datetime one hour ago
    one_hour_ago = current_datetime - timedelta(minutes=10)
    start_time = one_hour_ago.strftime("%Y-%m-%dT%H:%M:%S") + offset_formatted


    requestBytesJson = {
        "aggregations": [{"metric": "io.confluent.kafka.server/sent_bytes"}],
        "filter": {
            "filters": [
                {"field": "resource.kafka.id", "op": "EQ", "value": args.resourceId},
                {"field": "metric.topic", "op": "EQ", "value": args.topic}
            ],
            "op": "AND"
        },
        "granularity": "PT1M",
        "intervals": [start_time + "/" + end_time],
        "limit": 10
    }

    requestRecordsJson = {
        "aggregations": [{"metric": "io.confluent.kafka.server/sent_records"}],
        "filter": {
            "filters": [
                {"field": "resource.kafka.id", "op": "EQ", "value": args.resourceId},
                {"field": "metric.topic", "op": "EQ", "value":  args.topic}
            ],
            "op": "AND"
        },
        "granularity": "PT1M",
        "intervals": [start_time + "/" + end_time],
        "limit": 10
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": "Basic " + auth_token
    }


    bytesResponse = requests.post("https://api.telemetry.confluent.cloud/v2/metrics/cloud/query",
                                  data=json.dumps(requestBytesJson), headers=headers)
    recordsResponse = requests.post("https://api.telemetry.confluent.cloud/v2/metrics/cloud/query",
                                    data=json.dumps(requestRecordsJson), headers=headers)

    # Check for HTTP errors and handle appropriately
    if bytesResponse.status_code == 401:
        print("Error: Authorization failed (401 Unauthorized). Please check your API key and secret.")
        print("The API key file may be invalid, expired, or you may not have permission to access this resource.")
        exit(1)
    elif bytesResponse.status_code == 403:
        print("Error: Access forbidden (403 Forbidden). You don't have permission to access this resource.")
        exit(1)
    elif bytesResponse.status_code == 404:
        print("Error: Resource not found (404 Not Found). Please check your resource ID and topic name.")
        exit(1)
    elif bytesResponse.status_code >= 400:
        print(f"Error: HTTP {bytesResponse.status_code} - {bytesResponse.reason}")
        print(f"Response: {bytesResponse.text}")
        exit(1)

    if recordsResponse.status_code == 401:
        print("Error: Authorization failed (401 Unauthorized). Please check your API key and secret.")
        print("The API key file may be invalid, expired, or you may not have permission to access this resource.")
        exit(1)
    elif recordsResponse.status_code == 403:
        print("Error: Access forbidden (403 Forbidden). You don't have permission to access this resource.")
        exit(1)
    elif recordsResponse.status_code == 404:
        print("Error: Resource not found (404 Not Found). Please check your resource ID and topic name.")
        exit(1)
    elif recordsResponse.status_code >= 400:
        print(f"Error: HTTP {recordsResponse.status_code} - {recordsResponse.reason}")
        print(f"Response: {recordsResponse.text}")
        exit(1)

    # Check if responses are successful
    bytesResponse.raise_for_status()
    recordsResponse.raise_for_status()

    # Parse the JSON data
    try:
        bytesJson = json.loads(bytesResponse.text)
        recordsJson = json.loads(recordsResponse.text)
    except json.JSONDecodeError as e:
        print(f"Error: Failed to parse JSON response: {e}")
        print(f"Bytes response text: {bytesResponse.text}")
        print(f"Records response text: {recordsResponse.text}")
        exit(1)

    bytes_result = calculate_values(bytesJson)
    records_results = calculate_values(recordsJson)
    print( build_human_summary(bytes_result, records_results) )

    return_json = {}
    return_json["bytes"] = { "raw_metrics": bytesJson, "calculations": bytes_result }
    return_json["records"] = { "raw_metrics": recordsJson, "calculations": records_results }

    print(json.dumps(return_json, indent=4, sort_keys=True))
    print(build_human_summary(bytes_result, records_results))