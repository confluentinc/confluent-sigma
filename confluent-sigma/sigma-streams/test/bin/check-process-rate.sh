#!/bin/bash

# These scripts are intended to be run from the test directory
#

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Script dir is $SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/../../bin/auto-configure.sh" ] ; then
  source $SCRIPT_DIR/../../bin/auto-configure.sh
fi

if [ ! -f "$SIGMA_CC_ADMIN" ] ; then
  echo "sigma cc admin properties not found."
  exit -1
fi

python3 "$SCRIPT_DIR"/check-process-rate.py