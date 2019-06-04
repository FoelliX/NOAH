#!/bin/bash

./arm-linux-androideabi-objdump -s --section=.rodata $1 > $2
