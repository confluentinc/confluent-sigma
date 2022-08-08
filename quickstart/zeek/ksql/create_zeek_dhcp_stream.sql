CREATE STREAM dhcp_stream ( 
ts DOUBLE(10,4), 
uids ARRAY<STRING>, 
client_addr VARCHAR, 
server_addr VARCHAR, 
mac VARCHAR, 
host_name STRING, 
requested_addr VARCHAR, 
assigned_addr VARCHAR, 
lease_time INTEGER, 
msg_types ARRAY<STRING>, 
duration INTEGER)
WITH (KAFKA_TOPIC='dhcp', VALUE_FORMAT='JSON');
