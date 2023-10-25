read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac


rm -r /data/fengwenhan/data/faultfuzz_hdfs_recovery/*
rm -r /data/fengwenhan/data/faultfuzz_hdfs/*
rm -r /data/fengwenhan/data/faultfuzz_hdfs_logs/*
rm -r /data/fengwenhan/data/faultfuzz_hdfs_jar_and_start/*