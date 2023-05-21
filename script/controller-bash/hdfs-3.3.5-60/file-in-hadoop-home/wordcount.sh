bin/hadoop fs -rm -r gyOutput
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.5.jar wordcount /user/root/gyInput /user/root/gyOutput
