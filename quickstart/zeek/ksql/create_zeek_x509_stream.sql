CREATE STREAM x509_stream ( 
ts DOUBLE(16,6), 
id STRING, 
"certificate.version" INTEGER, 
"certificate.serial" STRING, 
"certificate.subject" VARCHAR, 
"certificate.issuer" VARCHAR, 
"certificate.not_valid_before" BIGINT, 
"certificate.not_valid_after" BIGINT, 
"certificate.key_alg" STRING, 
"certificate.sig_alg" STRING, 
"certificate.key_type" STRING, 
"certificate.key_length" INTEGER, 
"certificate.exponent" INTEGER, 
"basic_constraints.ca" BOOLEAN, 
"basic_constraints.path_len" INTEGER) 
WITH (KAFKA_TOPIC='x509', VALUE_FORMAT='JSON');
