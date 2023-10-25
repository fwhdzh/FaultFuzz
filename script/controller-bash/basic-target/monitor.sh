mv console.txt $1

echo "This is bt monitor!"
echo $1

mkdir $1/monitor
cp /home/fengwenhan/data/phosphor_test/test_out/1.txt $1/monitor/

mkdir $1/fav-rst
mv /data/fengwenhan/data/faultfuzz_bt/inst_record/* $1/fav-rst/

mkdir $1/cov
# mkdir $1/cov/C1HM1
cp -r /data/fengwenhan/data/faultfuzz_bt/fuzzcov $1/cov
