insert into user(id, ctime, mtime, name, password, email, first_name, last_name)
values ('2b68e6133f5d47f7bd5b1de76b06eef6', 1584774807901, 1584774807901,
        'tuke', '$2a$10$eyXYnqfp/pLRKGgwSyzdwu9Fue1SHAFtpUQ4evfQnVxQUXPsUnxte',
        'tuke@dreamcat.org', 'Bobby', 'Strange');

insert into file(digest, type, size)
values ('7f9418207fb63284849891cc8a0dea80', 'text/plain', 37);

select *
from user;
select *
from user_file;
-- truncate user_file ; truncate file;

insert into user_file(pid, name, id, ctime, mtime)
values ('2b68e6133f5d47f7bd5b1de76b06eef6', '/', '1', 1584774807901, 1584774807901);
insert into user_file(pid, name, id, ctime, mtime)
values ('1', 'a', '2', 1584774807901, 1584774807901);
insert into user_file(pid, name, id, ctime, mtime)
values ('1', 'b', '3', 1584774807901, 1584774807901);
insert into user_file(pid, name, id, ctime, mtime)
values ('2', 'c', '4', 1584774807901, 1584774807901);

select *
from user_file
where id = '1';
select *
from user_file
where pid = '1'
  and name in ('a', 'b');

-- rename /b to /new_b
begin
    batch
    delete from user_file where pid = '1' and name = 'b' if exists;
    insert into user_file(pid, name, id, ctime, mtime)
    values ('1', 'new_b', '3', 1584774807901, 1584774807901) if not exists;
    apply batch;

-- move /new_b to /a
insert into user_file(pid, name, id, ctime, mtime)
values ('2', 'new_b', '3', 1584774807901, 1584774807901) if not exists;
delete
from user_file
where pid = '1'
  and name = 'new_b' if exists;

-- copy /a/new_b to /a/new_b_copy
insert into user_file(pid, name, id, ctime, mtime)
values ('2', 'new_b_copy', '4', 1584774807901, 1584774807901) if not exists;
select *
from user_file
where pid = '3';

-- remove
begin
    batch
    delete from user_file where id = 'x';
    delete from user_file where id = 'y';
    apply batch;

-- pageable
select *
from user_file
where token(pid) >= token('2')
limit 2;


-- atomic
select *
from id_generator;
insert into id_generator(id)
values (1) if not exists;

create table t (
    id   int primary key,
    name text,
);
create index if not exists idx_t_name on t (name);

select *
from t;
insert into t(id, name)
values (1, 'a');
insert into t(id, name)
values (2, 'a');
insert into t(id, name)
values (3, 'a');
insert into t(id, name)
values (4, 'a');
insert into t(id, name)
values (5, 'b');

select *
from t
where name = 'a';
select *
from t
where token(id) > token(1)
  and token(id) < token(3);

delete
from t
where token(id) > token(1)
  and token(id) < token(3);
delete
from t
where id in (1, 2, 3);


