import os
import subprocess
import json
import requests
import pytz

from datetime import datetime, timedelta

SIGMA_CC_ADMIN = os.environ.get('SIGMA_CC_ADMIN')

if not os.path.isfile(SIGMA_CC_ADMIN):
    print("sigma cc admin properties not found.")
    exit(-1)

with open(SIGMA_CC_ADMIN) as f:
    for line in f:
        if line.startswith("rest.auth.token="):
            AUTH_TOKEN = line.split("=")[1].strip()
            break


current_datetime = datetime.now(pytz.timezone("America/New_York"))

offset = current_datetime.strftime("%z")
offset_formatted = f"{offset[:-2]}:{offset[-2:]}"

end_time = current_datetime.strftime("%Y-%m-%dT%H:%M:%S") + offset_formatted

# Calculate the datetime one hour ago
one_hour_ago = current_datetime - timedelta(hours=1)
start_time = one_hour_ago.strftime("%Y-%m-%dT%H:%M:%S") + offset_formatted
resource_id = "lkc-7yyp22"

requestBytesJson = {
    "aggregations": [{"metric": "io.confluent.kafka.server/sent_bytes"}],
    "filter": {
        "filters": [
            {"field": "resource.kafka.id", "op": "EQ", "value": resource_id},
            {"field": "metric.topic", "op": "EQ", "value": "test2"}
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
            {"field": "resource.kafka.id", "op": "EQ", "value": resource_id},
            {"field": "metric.topic", "op": "EQ", "value": "test2"}
        ],
        "op": "AND"
    },
    "granularity": "PT1M",
    "intervals": [start_time + "/" + end_time],
    "limit": 10
}

headers = {
    "Content-Type": "application/json",
    "Authorization": "Basic " + AUTH_TOKEN
}


bytesResponse = requests.post("https://api.telemetry.confluent.cloud/v2/metrics/cloud/query", data=json.dumps(requestBytesJson), headers=headers)
recordsResponse = requests.post("https://api.telemetry.confluent.cloud/v2/metrics/cloud/query", data=json.dumps(requestRecordsJson), headers=headers)


# Parse the JSON data
bytesJson = json.loads(bytesResponse.text)
recordsJson = json.loads(recordsResponse.text)

# Pretty print the parsed JSON data
print("bytes sent from cluster to Confluent Sigma")
print(json.dumps(bytesJson, indent=4, sort_keys=True))
print('\n' + "records sent from cluster to Confluent Sigma")
print(json.dumps(recordsJson, indent=4, sort_keys=True))
