echo "C1NN:"
docker exec -t C1NN /bin/bash -ic 'jps'
printf " \n"
echo "C1RM:"
docker exec -t C1RM /bin/bash -ic 'jps'
printf " \n"
echo "C1Master1:"
docker exec -t C1Master1 /bin/bash -ic 'jps'
printf "\n"
echo "C1Master2:"
docker exec -t C1Master2 /bin/bash -ic 'jps'
printf "\n"
echo "C1Slave1:"
docker exec -t C1Slave1 /bin/bash -ic 'jps'
printf "\n"
echo "C1Slave2:"
docker exec -t C1Slave2 /bin/bash -ic 'jps'
printf "\n"
echo "C1Slave3:"
docker exec -t C1Slave3 /bin/bash -ic 'jps'
printf "\n"
echo "C1Slave4:"
docker exec -t C1Slave4 /bin/bash -ic 'jps'
printf "\n"
echo "C1HS:"
docker exec -t C1HS /bin/bash -ic 'jps'
printf "\n"
echo "C1WPS:"
docker exec -t C1WPS /bin/bash -ic 'jps'
printf "\n"
echo "C1hd-zk:"
docker exec -t C1hd-zk /bin/bash -ic 'jps'
printf "\n"
