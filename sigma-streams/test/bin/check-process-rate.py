#!python3

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

import json
import requests
import pytz
import statistics
import argparse

from datetime import datetime, timedelta

import os

SIGMA_PROPS_FILENAME = "sigma.properties"
SIGMA_CC_ADMIN_FILENAME = "sigma-cc-admin.properties"


# This function is the equivalent of the auto-configure bash shell script.  We may want to rework that script to use
# this python routine, so we maintain the location searching in one place.
def get_sigma_config_variables():
    # Set variables

    sigma_props_path = None
    cc_admin_path = None

    # Check for sigma.properties file in default paths
    if os.path.isfile(os.path.expanduser("~/.sigma/" + SIGMA_PROPS_FILENAME)):
        sigma_props_path = os.path.expanduser("~/.sigma/" + SIGMA_PROPS_FILENAME)
    elif os.path.isfile(os.path.expanduser("~/.config/" + SIGMA_PROPS_FILENAME)):
        sigma_props_path = os.path.expanduser("~/.config/" + SIGMA_PROPS_FILENAME)
    elif os.path.isfile(os.path.expanduser("~/.confluent/" + SIGMA_PROPS_FILENAME)):
        sigma_props_path = os.path.expanduser("~/.confluent/" + SIGMA_PROPS_FILENAME)

    # Check for sigma-cc-admin.properties file in default paths
    if os.path.isfile(os.path.expanduser("~/.sigma/" + SIGMA_CC_ADMIN_FILENAME)):
        cc_admin_path = os.path.expanduser("~/.sigma/" + SIGMA_CC_ADMIN_FILENAME)
    if os.path.isfile(os.path.expanduser("~/.config/" + SIGMA_CC_ADMIN_FILENAME)):
        cc_admin_path = os.path.expanduser("~/.config/" + SIGMA_CC_ADMIN_FILENAME)
    elif os.path.isfile(os.path.expanduser("~/.confluent/" + SIGMA_CC_ADMIN_FILENAME)):
        cc_admin_path = os.path.expanduser("~/.confluent/" + SIGMA_CC_ADMIN_FILENAME)

    if sigma_props_path == None:
        raise Exception("Unable to find sigma properties")

    if cc_admin_path == None:
        raise Exception("Unable to find confluent cloud admin properties")

    return sigma_props_path, cc_admin_path


def calculate_values(json_metrics):

    values = [item['value'] for item in json_metrics['data']]

    if values is None or len(values) < 3:
        print("Error: Either metrics API returned no data points or there was less than three. Most likely cause is "
              "that Confluent Sigma is not running")
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

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="retrieve bytes and record send data")

    # Required parameters
    parser.add_argument("resourceId", help="resource for the confluent cluster")
    parser.add_argument("topic", help="topic you want a record count for")

    args = parser.parse_args()

    config_paths = get_sigma_config_variables()
    sigma_cc_admin = config_paths[1]

    if sigma_cc_admin is None or not os.path.isfile(sigma_cc_admin):
        print("sigma cc admin properties not found.")
        exit(-1)

    with open(sigma_cc_admin) as f:
        for line in f:
            if line.startswith("rest.auth.token="):
                auth_token = line.split("=")[1].strip()
                break

    if auth_token is None:
        raise Exception("Can't find rest.auth.token in file " + sigma_cc_admin)

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


    # Parse the JSON data
    bytesJson = json.loads(bytesResponse.text)
    recordsJson = json.loads(recordsResponse.text)

    bytes_result = calculate_values(bytesJson)
    records_results = calculate_values(recordsJson)
    print( build_human_summary(bytes_result, records_results) )

    return_json = {}
    return_json["bytes"] = { "raw_metrics": bytesJson, "calculations": bytes_result }
    return_json["records"] = { "raw_metrics": recordsJson, "calculations": records_results }

    print(json.dumps(return_json, indent=4, sort_keys=True))
    print(build_human_summary(bytes_result, records_results))