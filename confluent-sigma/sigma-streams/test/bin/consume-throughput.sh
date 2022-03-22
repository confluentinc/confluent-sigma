#!/bin/bash

if [ -f /tmp/ccloud-env.sh ] ; then
  source /tmp/ccloud-env.sh
elif [ -f ~/tmp/ccloud-env.sh ] ; then
    source  ~/tmp/ccloud-env.sh
else
  echo "ccloud-env not found"
  exit
fi

PROPS=

if [ -f ~/.config/sigma.properties ] ; then
  export PROPS=~/.config/
elif [ -f ~/.confluent/sigma.properties ] ; then
  export PROPS=~/.confluent/
elif [ -f ~/tmp/sigma.properties ] ; then
  export PROPS=~/tmp/
else
  echo "sigma properties not found"
  exit
fi

CGROUP="CONSUMER-PERF-$1-$RANDOM"

for i in `seq $2`
do	
  docker run -v ${PROPS}:/mnt/config --rm --network=host confluentinc/cp-server:latest \
       kafka-consumer-perf-test --bootstrap-server ${BOOTSTRAP_SERVER} \
       --messages 20000000 --consumer.config /mnt/config/sigma.properties --topic $1 --print-metrics --group $CGROUP > res$i.txt &
done
