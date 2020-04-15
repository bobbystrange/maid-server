#!/usr/bin/env bash

# single node
docker run --name cassandra -d -p 9041:9042 cassandra

# test cluster
docker run --name cassandra-node1 -d \
	-p 9042:9042 cassandra

# return container's vip
# $(docker inspect --format='{{.NetworkSettings.IPAddress}}' cassandra-node1)
docker run --name cassandra-node2 -d \
    --link cassandra-node1 \
	-e CASSANDRA_SEEDS="cassandra-node1" \
	-p 9142:9042 cassandra

docker run --name cassandra-node3 -d \
    --link cassandra-node1 \
	-e CASSANDRA_SEEDS="cassandra-node1" \
	-p 9242:9042 cassandra

docker exec cassandra-node1 nodetool status
docker exec -it cassandra-node1 cqlsh

#docker stop cassandra-node1 cassandra-node2 cassandra-node3
#docker start cassandra-node1 cassandra-node2 cassandra-node3
#docker rm -f cassandra-node1 cassandra-node2 cassandra-node3
