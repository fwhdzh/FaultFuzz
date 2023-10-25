read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac

rm -r /home/fengwenhan/data/faultfuzz_hbase_recovery/*
rm -r /home/fengwenhan/data/faultfuzz_hbase/*
rm -r /home/fengwenhan/data/faultfuzz_hbase_logs/*
rm -r /home/fengwenhan/data/faultfuzz_hbase_jar_and_start/*