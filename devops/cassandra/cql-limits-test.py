#!/usr/bin/env python3

'''
pip install -U cassandra-driver

create keyspace if not exists ks
            with replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

use ks;

create table if not exists user_file (
    path   text,
    items  set<text>,
    primary key ( path )
);

'''

from cassandra.cluster import Cluster

cluster = Cluster(contact_points=['127.0.0.1'], port=9041)

# session = cluster.connect()
# session.set_keyspace('keyspacename')
# # or session.execute('use keyspacename')
session = cluster.connect('ks')

rows = session.execute('select * from user_file')
for row in rows:
    path = row[0]
    items = row[1]
    print(path, len(items))

# rows = session.execute('select * from user_file')
# for (path, items) in rows:
#     print(path, items)

# select * from user_file;
# delete from user_file where path = '/';
cql = "insert into user_file(path, items) values ('/', {'%s'})" \
      % "','".join([str(i) for i in range(65536)])
session.execute(cql)

rows = session.execute('select * from user_file')
for (path, items) in rows:
    print(path, len(items))

cluster.shutdown()
