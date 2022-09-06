#!/bin/bash

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

export SIGMAPROPS=$PROPS