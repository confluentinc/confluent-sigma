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

# Script for launching the sigma rules loader

# first check the default locations for sigma.properties.

PROPS=

if [ -f "$1" ] ; then
  PROPS=$1
elif [ -n "$SIGMAPROPS" ] ; then
  PROPS=$SIGMAPROPS
elif [ -f ~/.config/sigma.properties ] ; then
  PROPS=~/.config/sigma.properties
elif [ -f ~/.confluent/sigma.properties ] ; then
  PROPS=~/.confluent/sigma.properties
elif [ -f ~/tmp/sigma.properties ] ; then
  PROPS=~/tmp/sigma.properties
fi

java -cp sigma-streams-1.2.0-fat.jar io.confluent.sigmarules.tools.SigmaRuleLoader $* -c $PROPS