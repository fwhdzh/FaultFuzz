echo "C1HM1:"
docker exec -t C1HM1 /bin/bash -ic 'jps'
printf " \n"
echo "C1HM2:"
docker exec -t C1HM2 /bin/bash -ic 'jps'
printf " \n"
echo "C1RS1:"
docker exec -t C1RS1 /bin/bash -ic 'jps'
printf "\n"
echo "C1RS2:"
docker exec -t C1RS2 /bin/bash -ic 'jps'
printf "\n"
echo "C1RS3:"
docker exec -t C1RS3 /bin/bash -ic 'jps'
printf "\n"
echo "C1hb-zk:"
docker exec -t C1hb-zk /bin/bash -ic 'jps'
printf "\n"
echo "C1hb-hdfs:"
docker exec -t C1hb-hdfs /bin/bash -ic 'jps'
printf "\n"
echo "C1hb-cli:"
docker exec -t C1hb-cli /bin/bash -ic 'jps'
printf "\n"
