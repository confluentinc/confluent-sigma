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

if [ -f bin/auto-configure.sh ] ; then
  source bin/auto-configure.sh
elif [ -f auto-configure.sh ] ; then
  source auto-configure.sh
fi

# After running auto-configure we will check and see if a properties file is passed in as the parameter
# if so then this should be used rather than whats found in the path

if [ $# -gt 0 ] ; then
  if [ -f $1 ] ; then
    echo "foo $1"
    SIGMA_PROPS=$1
  fi
fi

echo "Using properties $SIGMA_PROPS, and jar $SIGMA_JAR"

if [ -f $SIGMA_PROPS ] ; then
  java -cp $SIGMA_JAR io.confluent.sigmarules.SigmaStreamsApp -c $SIGMA_PROPS
else
  java -cp $SIGMA_JAR io.confluent.sigmarules.SigmaStreamsApp
fi