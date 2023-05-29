OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker-compose -p fav-zk -f $OWN_DIR/zk-compose.yaml up -d

firewall-cmd --permanent --zone=public --add-rich-rule='rule family=ipv4 source address=172.30.0.0/16 accept'
firewall-cmd --reload
