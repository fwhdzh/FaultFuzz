
function start {
        #nodeName=$(docker network inspect hadoop | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        nodeName=""
        daemonName=""
		daemon2Name=""
        case $1 in
           172.42.0.2)
              nodeName="C1NN"
              daemonName="namenode"
			  daemon2Name="zkfc"
              ;;
           172.42.0.4)
              nodeName="C1Master1"
              daemonName="namenode"
			  daemon2Name="zkfc"
              ;;
           172.42.0.5)
              nodeName="C1Master2"
              daemonName="namenode"
			  daemon2Name="zkfc"
              ;;
           172.42.0.6)
              nodeName="C1Slave1"
              daemonName="datanode"
			  daemon2Name="journalnode"
              ;;
		   172.42.0.7)
              nodeName="C1Slave2"
              daemonName="datanode"
			  daemon2Name="journalnode"
              ;;
		   172.42.0.8)
              nodeName="C1Slave3"
              daemonName="datanode"
			  daemon2Name="journalnode"
              ;;
		   172.42.0.9)
              nodeName="C1Slave4"
              daemonName="datanode"
			  daemon2Name="journalnode"
              ;;
        esac

        docker start $nodeName
        docker exec -t $nodeName /bin/bash -ic '/etc/init.d/ssh start'
        docker cp hdhosts $nodeName:/etc/
        docker exec -t $nodeName /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

        docker exec -t $nodeName /bin/bash -ic "iptables-restore < /home/gaoyu/iptables-rules"
		
		  docker exec -t $nodeName /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.5/ && bin/hdfs --daemon start '$daemonName' && jps'
		  docker exec -t $nodeName /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.5/ && bin/hdfs --daemon start '$daemon2Name' && jps'

        sh waitNormal.sh $nodeName
}

start $1
