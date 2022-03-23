#!/bin/bash

# Configure variables by search default paths for a script to set ccloud-env and also command config
# source this script ot use it

if [ -f /tmp/ccloud-env.sh ] ; then
  source /tmp/ccloud-env.sh
elif [ -f ~/tmp/ccloud-env.sh ] ; then
  source  ~/tmp/ccloud-env.sh
else
  echo "ccloud-env not found"
  exit
fi

PROPS=
PROPS_DIR=

if [ -f ~/.config/sigma.properties ] ; then
  export PROPS_DIR=~/.config/
  export PROPS=~/.config/sigma.properties
elif [ -f ~/.confluent/sigma.properties ] ; then
  export PROPS_DIR=~/.confluent/
  export PROPS=~/.confluent/sigma.properties
elif [ -f ~/tmp/sigma.properties ] ; then
  export PROPS_DIR=~/tmp/
  export PROPS=~/tmp/sigma.properties
else
  echo "sigma properties not found"
  exit
fi
