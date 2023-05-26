START_TIME=`date +%s`

workdir=$(pwd)

echo "This is bt crash!"

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME