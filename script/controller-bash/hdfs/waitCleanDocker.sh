
function waitClean(){
start=$(date +%s)

while [ 0 -le 5 ]
do

node=$1
info=$2
rst=$(docker exec -t $node bin/bash -ic "jps | grep -v "Jps"")
if [[ "$rst" == "" ]]; then
#  echo "!!!!!!!!!!!!wait master info $node:$rst"
#  echo "$rst"
  break

fi

end=$(date +%s)
#take=$(( end - start ))
let start_=10#${start}
let end_=10#${end}
take=$(( end_ - start_ ))/60

if [[ $take -ge 10 ]]; then
  sh failTest.sh "The docker was not cleaned in 10 mins!"
  sh jpsCluster.sh
  exit 0
  break
else
  docker exec -t $node /bin/bash -ic "pkill -9 -u root && jps"
fi

done
}

waitClean $1
