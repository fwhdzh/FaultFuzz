OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

monitor_path=$1

#mkdir $monitor_path
#sh monitor.sh $monitor_path
#rm -rf $monitor_path/C1hb-hdfs
#cp -r record-state $monitor_path

exceptions=$( grep -nr "Exception" $(find $monitor_path -name "*.out") | grep -v "ConnectException" | grep -v "ConnectionClosedException" | grep -v "ConnectTimeoutException" | grep -v "Failed get of master address: java.io.IOException" | grep -v "EOFException" | grep -v "Exception.<init>" | grep -v "CallTimeoutException" | grep -v "EndOfStreamException" | grep -v "IOException.<init>" | grep -v "SocketException" | grep -v "InterruptedException" | grep -v "Exception when using channel" | grep -v "IOException" | grep -v "Exception when following the leader" | grep -v "SocketTimeoutException" | grep -v "Exception while shutting down" | grep -v "NoRouteToHostException" | grep -v "Exception occurred while instrumenting" | grep -v "KeeperException" | grep -v  "InstanceAlreadyExistsException")
#exceptions=$( echo $exceptions | grep -v "ConnectException" )
#exceptions=$( echo $exceptions | grep -v "ConnectionClosedException" )
#repeated consistently
#repeatException=$( echo $exceptions | grep "repeated consistently" | awk 'NR==1')
#if [ "$repeatException" != "" ]; then
#only keep the exception when it is repeated.
#exceptions=$( echo $exceptions | grep -v "$repeatException" )
#fi

if [ "$exceptions" != "" ]; then
  sh $OWN_DIR/failTest.sh "Got exceptions: $exceptions"
else
  echo "No buggy exceptions!"
fi

fatalfile=$(find $monitor_path -name "*.log")
if [ "$fatalfile" != "" ]; then
  echo "fatalfile is not null"
  fatal=$( grep -nr "FATAL" $fatalfile )
  if [ "$fatal" != "" ]; then
    sh $OWN_DIR/failTest.sh "Got FATAL: $fatal"
  else
    echo "No FATAL!"
  fi
else 
  echo "there is no fatal file"
fi

errorfile=$(find $monitor_path -name "*.log")
if [ "$errorfile" != "" ]; then
  echo "errorfile is not null"
  error=$( grep -nr "ERROR" $errorfile )
  
  if [ "$error" != "" ]; then
    repeatError=$( echo $error | grep "repeated consistently" | awk 'NR==1')
    if [ "$repeatError" != "" ]; then
      #only keep the error when it is repeated.
      error=$( echo $error | grep -v "$repeatError" )
    fi
    sh $OWN_DIR/failTest.sh "Got ERROR: $error"
  else
    echo "No ERROR!"
  fi
else 
  echo "there is no error file"
fi

# fatal=$( grep -nr "FATAL" $(find $monitor_path -name "*.log") )

# error=$( grep -nr "ERROR" $(find $monitor_path -name "*.log") )
# #error=$( echo $error | grep -v "ClassCircularityError" )
# repeatError=$( echo $error | grep "repeated consistently" | awk 'NR==1')
# if [ "$repeatError" != "" ]; then
# #only keep the error when it is repeated.
# error=$( echo $error | grep -v "$repeatError" )
# fi

# if [ "$exceptions" != "" ]; then
#   sh $OWN_DIR/failTest.sh "Got exceptions: $exceptions"
# else
#   echo "No buggy exceptions!"
# fi

# if [ "$fatal" != "" ]; then
#   sh $OWN_DIR/failTest.sh "Got FATAL: $fatal"
# else
#   echo "No FATAL!"
# fi

# if [ "$error" != "" ]; then
#   sh $OWN_DIR/failTest.sh "Got ERROR: $error"
# else
#   echo "No ERROR!"
# fi

# #rm -rf $monitor_path