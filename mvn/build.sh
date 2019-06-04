#!/bin/bash

cd target/build
mkdir results
mkdir data/temp

zip -u $1 tool.properties
rm tool.properties