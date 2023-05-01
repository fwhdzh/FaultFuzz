function checkAlive(){
   node=$1
   character=$2
   workerRst=$(docker exec -t $node /bin/bash -ic 'jps')
   result=$(echo $workerRst | grep "${character}")
   if [[ "$result" != "" ]]; then
         if [[ "$rtn" == "" ]]; then
		     rtn="$node:11181"
	 else
		     rtn="$rtn,$node:11181"
	 fi
   fi
 }
 
 rtn=""
 
 function checkAllAlive(){
   checkAlive "C1ZK1" "QuorumPeerMain"
   checkAlive "C1ZK2" "QuorumPeerMain"
   checkAlive "C1ZK3" "QuorumPeerMain"
   checkAlive "C1ZK4" "QuorumPeerMain"
   checkAlive "C1ZK5" "QuorumPeerMain"
 }

checkAllAlive

echo "$rtn"