alter table readme_config add path_mode varchar(32);
update readme_config set path_mode = 'relative' where path_mode is null;