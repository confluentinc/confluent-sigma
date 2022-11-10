SELECT 
s.TS AS SSL_TS, 
s."id.orig_h" AS SRC_IP, 
s."id.orig_p" AS SRC_PORT, 
s."id.resp_h" AS DEST_IP, 
getgeoforip("id.resp_h") AS GEOIP,
getasnforip("id.resp_h") AS ASNIP,
s."id.resp_p" AS DEST_PORT, 
s.VERSION AS VERSION, 
s.CIPHER AS CIPHER, 
s.CURVE AS CURVE, 
s.SERVER_NAME AS SERVER_NAME, 
s.SUBJECT AS SUBJECT, 
s.ISSUER AS ISSUER, 
s.VALIDATION_STATUS AS VALIDATION_STATUS, 
x.TS AS X509_TS, 
x."certificate.version" AS CERTIFICATE_VERSION, 
x."certificate.not_valid_before" AS CERTIFICATE_NOT_VALID_BEFORE, 
x."certificate.not_valid_after" AS CERTIFICATE_NOT_VALID_AFTER, 
TIMESTAMPTOSTRING((x."certificate.not_valid_after" * 1000), 'yyyy-MM-dd HH:mm:ss') AS CERT_EXPIRATION_DATE, 
TIMESTAMPTOSTRING((x."certificate.not_valid_before" * 1000), 'yyyy-MM-dd HH:mm:ss') AS CERT_REGISTRATION_DATE, 
x."certificate.key_alg" AS CERTIFICATE_KEY_ALG, 
x."certificate.sig_alg" AS CERTIFICATE_SIG_ALG, 
x."certificate.key_type" AS CERTIFICATE_KEY_TYPE, 
x."certificate.key_length" AS CERTIFICATE_KEY_LENGTH 
FROM SSL_STREAM s INNER JOIN X509_STREAM x WITHIN 1 SECONDS 
on s.SUBJECT = x."certificate.subject" 
WHERE s.VALIDATION_STATUS!='ok' EMIT CHANGES;
