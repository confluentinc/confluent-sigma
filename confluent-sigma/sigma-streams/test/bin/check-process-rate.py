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

print("Formatted: ", start_time)
print("Formatted: ", end_time)

#start_time = "2023-06-07T15:28:00-04:00"
#end_time = "2023-06-07T16:28:00-04:00"
resource_id = "lkc-7yyp22"

data = {
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
    "limit": 1000
}

print(json.dumps(data) + '\n')

headers = {
    "Content-Type": "application/json",
    "Authorization": "Basic " + AUTH_TOKEN
}


response = requests.post("https://api.telemetry.confluent.cloud/v2/metrics/cloud/query", data=json.dumps(data), headers=headers)

print(response.text)