#!/usr/bin/env bash

# Script assembles the quick start from the dependent repositories

rm -rf target/*
mkdir target/quickstart
git clone https://github.com/confluentinc/demo-siem-optimization.git target/quickstart
rm target/quickstart/README.md
rm -rf target/quickstart/.git
find target/quickstart -name '.git*' -delete

