read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac


rm -r /home/fengwenhan/data/crashfuzz_hdfs_recovery/*
rm -r /home/fengwenhan/data/crashfuzz_hdfs/*
rm -r /home/fengwenhan/data/crashfuzz_hdfs_logs/*
rm -r /home/fengwenhan/data/crashfuzz_hdfs_jar_and_start/*