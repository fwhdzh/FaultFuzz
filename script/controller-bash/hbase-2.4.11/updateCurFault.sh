docker exec -t C1HM1 /bin/bash -ic 'rm /home/gaoyu/hb2411curFault'
docker cp hb2411curFault C1HM1:/home/gaoyu
docker exec -t C1HM2 /bin/bash -ic 'rm /home/gaoyu/hb2411curFault'
docker cp hb2411curFault C1HM2:/home/gaoyu
docker exec -t C1RS1 /bin/bash -ic 'rm /home/gaoyu/hb2411curFault'
docker cp hb2411curFault C1RS1:/home/gaoyu
docker exec -t C1RS2 /bin/bash -ic 'rm /home/gaoyu/hb2411curFault'
docker cp hb2411curFault C1RS2:/home/gaoyu
docker exec -t C1RS3 /bin/bash -ic 'rm /home/gaoyu/hb2411curFault'
docker cp hb2411curFault C1RS3:/home/gaoyu
