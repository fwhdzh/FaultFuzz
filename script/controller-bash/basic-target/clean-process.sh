process_names="BTMain"

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
