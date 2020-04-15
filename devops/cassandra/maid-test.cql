
insert into user(id, ctime, mtime, name, password, email, first_name, last_name)
values (2b68e613-3f5d-47f7-bd5b-1de76b06eef6, 1584774807901, 1584774807901,
'tuke', '$2a$10$eyXYnqfp/pLRKGgwSyzdwu9Fue1SHAFtpUQ4evfQnVxQUXPsUnxte',
'tuke@dreamcat.org', 'Bobby', 'Strange');

-- truncate user_file ; truncate file
insert into user_file(uid, path, ctime, mtime)
values (2b68e613-3f5d-47f7-bd5b-1de76b06eef6, '/', 1584774807901, 1584774807901);

select * from user;
select * from user_file;
select * from user_file
where path in ('/','/test') and uid=2b68e613-3f5d-47f7-bd5b-1de76b06eef6;

insert into file(digest, type, size)
values ('7f9418207fb63284849891cc8a0dea80', 'text/plain', 37)
