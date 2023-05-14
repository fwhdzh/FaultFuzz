read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac

rm -r /data/fengwenhan/data/crashfuzz_hbase_recovery/*
rm -r /data/fengwenhan/data/crashfuzz_hbase/*
rm -r /data/fengwenhan/data/crashfuzz_hbase_logs/*
rm -r /data/fengwenhan/data/crashfuzz_hbase_jar_and_start/*