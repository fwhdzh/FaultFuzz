function start {
        #nodeName=$(docker network inspect hadoop | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        nodeName=""
        daemonName=""
        case $1 in
           172.27.0.2)
              nodeName="C1HM1"
              daemonName="master"
              ;;
           172.27.0.3)
              nodeName="C1HM2"
              daemonName="master"
              ;;
           172.27.0.4)
              nodeName="C1RS1"
              daemonName="regionserver"
              ;;
           172.27.0.5)
              nodeName="C1RS2"
              daemonName="regionserver"
              ;;
           172.27.0.6)
              nodeName="C1RS3"
              daemonName="regionserver"
              ;;
        esac

        docker start $nodeName
        docker exec -t $nodeName /bin/bash -ic '/etc/init.d/ssh start'
        docker cp hbhosts $nodeName:/etc/
        docker exec -t $nodeName /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"
        
        docker exec -t $nodeName /bin/bash -ic "iptables-restore < /home/gaoyu/iptables-rules"

        docker exec -t $nodeName /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.11/ && bin/hbase-daemon.sh start '$daemonName' && jps'

        sh waitNormal.sh $nodeName
}

start $1
