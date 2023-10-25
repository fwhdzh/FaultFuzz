function wait() {
    jpsRst=$(docker exec -t $1 /bin/bash -ic 'jps')
    info="main class information unavailable"
    rst=$(echo $jpsRst| grep "${info}")
    while [ "$rst" != "" ]
    do
        sleep 10
        jpsRst=$(docker exec -t $1 /bin/bash -ic 'jps')
        rst=$(echo $jpsRst| grep "${info}")
    done
    sleep 15
}
wait $1
