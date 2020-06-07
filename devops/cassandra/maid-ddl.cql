-- keyspace
drop keyspace if exists maid;
create keyspace if not exists maid
            with replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
use maid;

-- table
-- desc user
create table if not exists user (
    id         bigint primary key,
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
    uid    bigint,
    ctime  bigint,
    avatar text,
    primary key (uid, ctime),
);

create table if not exists file (
    digest text primary key,
    type   text,
    size   bigint,
);

create table if not exists user_file (
    uid    bigint,
    pid    bigint,
    name   text,
    id     bigint,

    ctime  bigint,
    mtime  bigint,
    digest text,
    type   text,
    size   bigint,
    primary key ( uid, pid, name )
);
create index if not exists idx_user_file_id on user_file (id);

create table if not exists share_file (
    id       bigint primary key,
    uid      bigint,
    fid      bigint,

    ctime    bigint,
    password text,
    ttl      bigint,
);
create index if not exists idx_share_file_uid on share_file (uid);
create index if not exists idx_share_file_fid on share_file (fid);

create table if not exists id_generator (
    id bigint primary key
);


/// maid hub

create table if not exists dict (
    key   text,
    name  text,
    value text,
    primary key (key, name)
);

