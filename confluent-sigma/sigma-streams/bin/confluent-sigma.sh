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

# Script for launching the sigma streams app.  The only parameter that should be passed in is a properties file.  If
# this is not passed in then auto-configure.sh will find one in the default locations
#
# passing in -i to this script executes in interactive mode where you are prompted for parameters

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/auto-configure.sh
fi

if [ -f "$SIGMA_JAR" ] ; then
  echo "Found $SIGMA_JAR.  Using this for execution"
fi

# After running auto-configure we will check and see if a properties file is passed in as the parameter
# if so then this should be used rather than whats found in the path
if [ $# -gt 0 ] ; then
  if [ "$1" = '-i' ] ; then
    if [ -f "$SIGMA_JAR" ] ; then
      java -jar $SIGMA_JAR -c $SIGMA_PROPS
      exit
    else
      docker run -it confluentinc/confluent-sigma:1.3.0
      exit
    fi
  elif [ -f "$1" ] ; then
    SIGMA_PROPS=$1
    SIGMA_PROPS_DIR="$(dirname "$SIGMA_PROPS")"
    SIGMA_PROPS_FILENAME="$(basename "$SIGMA_PROPS")"
  fi
fi

if [ -f "$SIGMA_PROPS" ] ; then
  echo "Using properties $SIGMA_PROPS"
  if [ -f "$SIGMA_JAR" ] ; then
    java -jar $SIGMA_JAR -c $SIGMA_PROPS
  else
    docker run -v $SIGMA_PROPS_DIR:/conf confluentinc/confluent-sigma:1.3.0 -c /conf/$SIGMA_PROPS_FILENAME
  fi
else
  if [ -f "$SIGMA_JAR" ] ; then
    java -jar $SIGMA_JAR
  else
    docker run -it confluentinc/confluent-sigma:1.3.0
  fi
fi
