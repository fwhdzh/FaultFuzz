read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac

rm -r /data/fengwenhan/data/faultfuzz_bt/inst_cache/*
rm -r /data/fengwenhan/data/faultfuzz_bt/inst_record/*
rm -r /data/fengwenhan/data/faultfuzz_bt/inst_logs/*
rm -r /data/fengwenhan/data/faultfuzz_bt/fuzzcov/*
rm -r /data/fengwenhan/data/faultfuzz_bt/jar_and_start/*
rm -r /data/fengwenhan/data/faultfuzz_bt/logs/*
rm -r /data/fengwenhan/data/faultfuzz_bt/recovery/*
rm -r /data/fengwenhan/data/faultfuzz_bt/root/*