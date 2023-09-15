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

# This is the auto-configure script.  It will attempt to find the sigma.properties file in the default paths as well
# as finding a sigma jar file to use.  The end result should be exporting

# SIGMA_PROPS_FILENAME (the default name of the sigma properties file this script is looking for)
# SIGMA_PROPS (path to the properties file)
# SIGMA_PROPS_DIR (the directory the property files resides in)
# SIGMA_JAR (the jar files to use for running sigma)

export SIGMA_PROPS_FILENAME="sigma.properties"

SIGMA_PROPS=
SIGMA_PROPS_DIR=
SIGMA_CC_ADMIN=

if [ -f ~/.sigma/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/.sigma/
  export SIGMA_PROPS=~/.sigma/$SIGMA_PROPS_FILENAME
elif [ -f ~/.config/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/.config/
  export SIGMA_PROPS=~/.config/$SIGMA_PROPS_FILENAME
elif [ -f ~/.confluent/sigma.properties ] ; then
  export SIGMA_PROPS_DIR=~/.confluent/
  export SIGMA_PROPS=~/.confluent/$SIGMA_PROPS_FILENAME
fi

if [ -f ~/.config/sigma-cc-admin.properties ] ; then
  export SIGMA_CC_ADMIN=~/.config/sigma-cc-admin.properties
elif [ -f ~/.confluent/sigma-cc-admin.properties ] ; then
  export SIGMA_CC_ADMIN=~/.confluent/sigma-cc-admin.properties
elif [ -f ~/tmp/sigma-cc-admin.properties ] ; then
  export SIGMA_CC_ADMIN=~/tmp/sigma-cc-admin.properties
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

if [ ! -f "$SIGMA_JAR" ] ; then
  for TEST_FILE in ../target/sigma-streams-*-fat.jar
  do
    export SIGMA_JAR=$TEST_FILE
  done
fi

shopt -u nullglob
