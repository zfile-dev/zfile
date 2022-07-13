create table download_log_dg_tmp
(
    id          integer
        primary key autoincrement,
    path        text,
    storage_key varchar(64),
    create_time datetime,
    ip          varchar(64),
    user_agent  varchar(2048),
    referer     varchar(2048),
    short_key   varchar(255)
);

insert into download_log_dg_tmp(id, path, storage_key, create_time, ip, user_agent, referer, short_key)
select id,
       path,
       storage_key,
       create_time,
       ip,
       user_agent,
       referer,
       short_key
from download_log;

drop table download_log;

alter table download_log_dg_tmp
    rename to download_log;