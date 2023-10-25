function start {
        #nodeName=$(docker network inspect hadoop | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        nodeName=""
        daemonName=""
        case $1 in
           172.40.0.2)
              nodeName="C1ZK1"
              daemonName=""
              ;;
           172.40.0.3)
              nodeName="C1ZK2"
              daemonName=""
              ;;
           172.40.0.4)
              nodeName="C1ZK3"
              daemonName=""
              ;;
           172.40.0.5)
              nodeName="C1ZK4"
              daemonName=""
              ;;
           172.40.0.6)
              nodeName="C1ZK5"
              daemonName=""
              ;;
        esac

        docker start $nodeName
        docker cp zkhosts $nodeName:/etc/
        docker exec -t $nodeName /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

        docker exec -t $nodeName /bin/bash -ic "iptables-restore < /home/gaoyu/iptables-rules"

        docker exec -t $nodeName /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'
        sh waitNormal.sh $nodeName
}

start $1
#sleep 30s
