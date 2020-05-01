#!/usr/bin/env bash

# This image includes EXPOSE 2181 2888 3888 8080
# (the zookeeper client port, follower port, election port, AdminServer port respectively),
# so standard container linking will make it automatically available to the linked containers.
# Since the Zookeeper "fails fast" it's better to always restart it.

# single node
docker run --name zookeeper -d --restart always \
    -p 2180:2181 -p 21800:8080 \
    zookeeper


# test cluster
docker run --name zoo1 -d --restart always \
    -p 2181:2181 -p 21810:8080 \
    -e ZOO_MY_ID=1
    -e ZOO_SERVERS=server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    zookeeper

docker run --name zoo2 -d --restart always \
    -p 2182:2181 /
    -e ZOO_MY_ID=2
    -e ZOO_SERVERS=server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    zookeeper

docker run --name zoo3 -d --restart always \
    -p 2183:2181 /
    -e ZOO_MY_ID=3
    -e ZOO_SERVERS=server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    zookeeper

# or docker stack / docker-compose
docker-compose
docker stack deploy -c docker-compose.yml

###

# connect
docker exec -it zookeeper zkCli.sh
docker exec -it zoo1 zkCli.sh
