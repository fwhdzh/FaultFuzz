monitor_path=$1
#cp -r record-state $monitor_path

exceptions=$( grep -nr "Exception" $(find $monitor_path -name "*.log") | grep -v "ConnectException" | grep -v "ConnectionClosedException" | grep -v "ConnectTimeoutException" | grep -v "Failed get of master address: java.io.IOException" | grep -v "EOFException" | grep -v "at " | grep -v "CallTimeoutException" | grep -v "HttpGetFailedException" | grep -v "Exception.<init>" | grep -v "socket timeout exception" | grep -v "NoRouteToHostException" | grep -v "Timed out" | grep -v "ClosedChannelException" grep -v "SocketTimeoutException" | grep -v "WARN")
#exceptions=$( echo $exceptions | grep -v "ConnectException" )
#exceptions=$( echo $exceptions | grep -v "ConnectionClosedException" )
#repeated consistently
#repeatException=$( echo $exceptions | grep "repeated consistently" | awk 'NR==1')
#if [ "$repeatException" != "" ]; then
#only keep the exception when it is repeated.
#exceptions=$( echo $exceptions | grep -v "$repeatException" )
#fi

fatal=$( grep -nr "FATAL" $(find $monitor_path -name "*.log") )

error=$( grep -nr "ERROR" $(find $monitor_path -name "*.log") | grep -v "EditLog")
#error=$( echo $error | grep -v "ClassCircularityError" )
#repeatError=$( echo $error | grep "repeated consistently" | awk 'NR==1')
#if [ "$repeatError" != "" ]; then
#only keep the error when it is repeated.
#error=$( echo $error | grep -v "$repeatError" )
#fi

if [ "$exceptions" != "" ]; then
  sh failTest.sh "Got exceptions: $exceptions"
else
  echo "No buggy exceptions!"
fi

if [ "$fatal" != "" ]; then
  sh failTest.sh "Got FATAL: $fatal"
else
  echo "No FATAL!"
fi

if [ "$error" != "" ]; then
  sh failTest.sh "Got ERROR: $error"
else
  echo "No ERROR!"
fi

