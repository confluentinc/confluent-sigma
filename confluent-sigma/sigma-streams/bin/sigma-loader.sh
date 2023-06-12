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

if [ $# -eq 0 ]
then
    echo ""
    echo "Usage:"
    echo "sigma-loader.sh [-f sigma-rule.yml] [-d rules-directory] -c sigma.properties"
    echo ""
    echo "properties file passed to -c contains standard kafka connection properties.  This is likely to be the same as you are passing to confluent-sigma"
    echo ""
    exit 1
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/auto-configure.sh
fi

if [ -f "$SIGMA_JAR" ] ; then
  echo "Found $SIGMA_JAR.  Using this for execution"
else
  echo "Sigma jar not found"
  exit -1
fi

if [ -f $SIGMA_PROPS ] ; then
  java -cp $SIGMA_JAR io.confluent.sigmarules.tools.SigmaRuleLoader -c $SIGMA_PROPS $*
else
  java -cp $SIGMA_JAR io.confluent.sigmarules.tools.SigmaRuleLoader $*
fi
