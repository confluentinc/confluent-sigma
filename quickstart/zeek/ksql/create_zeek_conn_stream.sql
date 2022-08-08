CREATE STREAM conn_stream (
ts DOUBLE(16,6), 
uid STRING, 
"id.orig_h" VARCHAR, 
"id.orig_p" INTEGER, 
"id.resp_h" VARCHAR, 
"id.resp_p" INTEGER, 
proto STRING, 
service STRING, 
conn_state STRING, 
local_orig BOOLEAN, 
local_resp BOOLEAN, 
missed_bytes INTEGER, 
history STRING, 
orig_packets INTEGER, 
orig_ip_bytes INTEGER, 
resp_pkts INTEGER, 
resp_ip_bytes INTEGER) 
WITH (KAFKA_TOPIC='conn', VALUE_FORMAT='JSON');
