// prepare
insert into user(id, ctime, mtime, name, password, email, first_name, last_name)
values (43447782765232128, 1584774807901, 1584774807901,
        'tuke', '$2a$10$eyXYnqfp/pLRKGgwSyzdwu9Fue1SHAFtpUQ4evfQnVxQUXPsUnxte',
        'tuke@dreamcat.org', 'Bobby', 'Strange');

insert into avatar(uid, ctime, avatar) values(43447782765232128, 1584774807901,
'data:image/png;base64,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'
);

select * from user;
select * from user_file;
-- truncate user_file ; truncate file;

-- /a/d/e/, /a/d/f
-- /b/
-- /c/
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, -1, '/', 0, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, 0, 'a', 1, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, 0, 'b', 2, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, 0, 'c', 3, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, 1, 'd', 4, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime)
values (43447782765232128, 4, 'e', 5, 1584774807901, 1584774807901);
insert into user_file(uid, pid, name, id, ctime, mtime, digest, type, size)
values (43447782765232128, 4, 'f', 6, 1584774807901, 1584774807901, '3df5', 'text/plain', 10);

/// select

select * from user_file where id = 1;
select * from user_file where uid = 43447782765232128 and pid = 0;
select * from user_file where uid = 43447782765232128 and pid = 0  and name in ('a', 'b');

/// op

-- rename /b to /new_b
begin batch
    insert into user_file(uid, pid, name, id, ctime, mtime)
    values (43447782765232128, 0, 'new_b', 2, 1584774807901, 1584774807901) if not exists;
    delete from user_file
    where uid = 43447782765232128 and pid = 0 and name = 'b' if exists;
apply batch;

-- move /new_b to /a
begin batch
    delete from user_file
    where uid = 43447782765232128 and pid = 0 and name = 'new_b' if exists;
    insert into user_file(uid, pid, name, id, ctime, mtime)
    values (43447782765232128, 1, 'new_b', 2, 1584774807901, 1584774807901) if not exists;
apply batch;



-- copy /a/new_b to /a/new_b_copy
insert into user_file(pid, name, id, ctime, mtime)
values ('2', 'new_b_copy', '4', 1584774807901, 1584774807901) if not exists;

select * from user_file where pid = '3';

-- remove
begin batch
    delete from user_file where id = 'x';
    delete from user_file where id = 'y';
apply batch;

-- pageable
select * from user_file where token(pid) >= token('2') limit 2;


-- atomic
select * from id_generator;
insert into id_generator(id) values (1) if not exists;

