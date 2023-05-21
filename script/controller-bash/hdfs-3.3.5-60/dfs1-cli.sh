echo "------------------------------------------------------------------"
bin/hdfs dfs -mkdir -p /usr/root/fav1 /usr/root/fav2
rst=$?
echo "$(date "+%Y-%m-%d %H:%M:%S"): mkdir /usr/root/fav1 fav2: $rst"
if [ $rst != "0" ]; then
  sh ubuntuFailTest.sh "mkdir /usr/root/fav1 fav2 failed"
fi
echo "------------------------------------------------------------------"
bin/hdfs dfs -put gyInput/file01 /usr/root/fav2
rst=$?
echo "$(date "+%Y-%m-%d %H:%M:%S"): put gyInput/file01 /usr/root/fav2: $rst"
if [ $rst != "0" ]; then
  sh ubuntuFailTest.sh "put gyInput/file01 /usr/root/fav2 failed!"
fi
echo "------------------------------------------------------------------"
bin/hdfs dfs -truncate -w 23 /usr/root/fav2/file01
rst=$?
echo "$(date "+%Y-%m-%d %H:%M:%S"): -truncate -w 23 /usr/root/fav2/file01: $rst"
if [ $rst != "0" ]; then
  sh ubuntuFailTest.sh "-truncate -w 23 /usr/root/fav2/file01 failed!"
fi
echo "------------------------------------------------------------------"
