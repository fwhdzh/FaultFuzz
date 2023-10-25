#!/bin/bash

# 要查找的进程名字列表，以空格分隔
process_names="CloudFuzzMain"

# 遍历所有进程名字，查找并杀死相应进程
for name in $process_names
do
  echo "Looking for processes with name: $name"
  pids=$(jps | grep -E "$name$" | grep -v grep | awk '{print $1}')
  for pid in $pids
  do
    echo "Killing process $pid"
    kill $pid
  done
done