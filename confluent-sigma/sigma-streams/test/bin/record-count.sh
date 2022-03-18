 if [ -f /tmp/ccloud-env.sh ] ; then
   source /tmp/ccloud-env.sh
 elif [ -f ~/tmp/ccloud-env.sh ] ; then
     source  ~/tmp/ccloud-env.sh
 else
   echo "ccloud-env not found"
   exit
 fi

 docker run --rm --network=host edenhill/kafkacat:1.5.0  \
    kafkacat -b ${BOOTSTRAP_SERVER} -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username=${KAFKA_SASL_USERNAME} -X sasl.password=${KAFKA_SASL_PASSWORD} -t $1 -L