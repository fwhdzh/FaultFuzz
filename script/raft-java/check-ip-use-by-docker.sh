#!/bin/bash

# 获取当前所有Docker network名称
networks="$(docker network ls --format '{{.Name}}')"

# 循环遍历每个network，并查询其占用的端口
for network in $networks; do
  echo "Network: $network"
#   docker network inspect "$network" --format '{{range $k, $v := .Containers}}{{$k}} {{range $p, $conf := $v.Ports}}{{$p}} -> {{$conf.HostIp}}:{{$conf.HostPort}}; {{end}}{{end}}'
  info="$(docker network inspect $network)"
#   echo $info
  subnet="$(echo $info | jq -r '.[].IPAM.Config[].Subnet')"
  echo $subnet
done


