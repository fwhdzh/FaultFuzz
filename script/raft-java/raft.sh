docker exec -t dist-raft1 /bin/bash -ic ". /raft-start-server.sh example1 dist-raft1:12020:1,dist-raft2:12020:2,dist-raft3:12020:3 dist-raft1:12020:1"
docker exec -t dist-raft2 /bin/bash -ic ". /raft-start-server.sh example2 dist-raft1:12020:1,dist-raft2:12020:2,dist-raft3:12020:3 dist-raft2:12020:2"
docker exec -t dist-raft3 /bin/bash -ic ". /raft-start-server.sh example3 dist-raft1:12020:1,dist-raft2:12020:2,dist-raft3:12020:3 dist-raft3:12020:3"
