-- keyspace
drop keyspace if exists maid;
create keyspace if not exists maid
            with replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
use maid;

-- table
-- desc user
create table if not exists user (
    id         uuid primary key,
    ctime      bigint,
    mtime      bigint,
    name       text,
    password   text,
    email      text,
    first_name text,
    last_name  text,
);
create index if not exists idx_user_name on user (name);

create table if not exists avatar (
    uid    uuid,
    ctime  bigint,
    avatar text,
    primary key (uid, ctime),
);

create table if not exists user_file (
    uid    uuid,
    path   text,
    digest text,
    type   text,
    size   bigint,
    ctime  bigint,
    mtime  bigint,
    items  set<text>,
    primary key ( path, uid )
);

create table if not exists dictionary (
    key   text,
    name  text,
    value text,
    primary key (key, name)
);

create table if not exists file (
    digest text primary key,
    type   text,
    size   bigint,
);
