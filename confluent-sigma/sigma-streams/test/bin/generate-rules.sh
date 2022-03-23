#!/bin/bash

for i in `seq $1`
do
  sed "s/^title:.*$/title: test $i/" $2 > rules-$i.yml
done