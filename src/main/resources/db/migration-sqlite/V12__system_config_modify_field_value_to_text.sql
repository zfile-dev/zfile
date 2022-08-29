create table system_config_dg_tmp
(
    id    integer
        primary key autoincrement,
    name  varchar(255),
    value text,
    title varchar(255)
);

insert into system_config_dg_tmp(id, name, value, title)
select id, name, value, title
from system_config;

drop table system_config;

alter table system_config_dg_tmp
    rename to system_config;