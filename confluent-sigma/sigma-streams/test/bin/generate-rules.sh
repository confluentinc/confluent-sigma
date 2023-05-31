#!/bin/bash

# Takes two arguments.  The first one is the number of rules to generate and the second is the starter rules to
# duplicate

for i in `seq $1`
do
  sed "s/^title:.*$/title: test $i/" $2 > rules-$i.yml
done