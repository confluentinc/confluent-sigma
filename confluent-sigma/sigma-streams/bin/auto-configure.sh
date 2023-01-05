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

# define the default sigma properties file name
export SIGMA_PROPS_FILENAME="sigma.properties"

SIGMA_PROPS=
SIGMA_PROPS_DIR=

if [ -f ~/.config/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/.config/
  export SIGMA_PROPS=~/.config/$SIGMA_PROPS_FILENAME
elif [ -f ~/.confluent/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/.confluent/
  export SIGMA_PROPS=~/.confluent/$SIGMA_PROPS_FILENAME
elif [ -f ~/tmp/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/tmp/
  export SIGMA_PROPS=~/tmp/$SIGMA_PROPS_FILENAME
fi

shopt -s nullglob

SIGMA_JAR=
for TEST_FILE in sigma-streams-*-fat.jar
do
  export SIGMA_JAR=$TEST_FILE
done

if [ ! -f "$SIGMA_JAR" ] ; then
  for TEST_FILE in target/sigma-streams-*-fat.jar
  do
    export SIGMA_JAR=$TEST_FILE
  done
fi

shopt -u nullglob