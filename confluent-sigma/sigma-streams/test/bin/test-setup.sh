#!/bin/bash

# Assumes data already loaded

source bin/auto-configure.sh

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --command-config $PROPS --delete --topic sigma-rules
bin/sigma-loader.sh -dir test/rules/$1
