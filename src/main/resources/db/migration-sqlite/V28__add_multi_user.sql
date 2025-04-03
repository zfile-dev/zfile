create table if not exists user
(
    id                            integer primary key autoincrement,
    username                      varchar(255)  null,
    nickname                      varchar(255)  null,
    password                      varchar(32)   null,
    enable                        bit           null,
    create_time                   datetime      null,
    update_time                   datetime      null,
    default_permissions           text          null
);

create table if not exists user_storage_source
(
    id                            integer primary key autoincrement,
    user_id                       int           null,
    storage_source_id             int           null,
    root_path                     text          null,
    enable                        bit           null,
    permissions                    text          null
);

insert into user (id, username, nickname, password, enable, create_time) values (1, (select value from system_config where name = 'username'), '管理员', (select value from system_config where name = 'password'), true, datetime(CURRENT_TIMESTAMP,'localtime'));
insert into user (id, username, nickname, password, enable, create_time) values (2, 'guest', '匿名用户', null, true, datetime(CURRENT_TIMESTAMP,'localtime'));

-- 迁移管理员权限
insert into user_storage_source (user_id, storage_source_id, root_path, enable, permissions)
select 1, storage_id, '/', instr(group_concat(operator), 'available') > 0, group_concat(operator) permissions from permission_config
where allow_admin = true
group by storage_id;

-- 迁移匿名用户权限
insert into user_storage_source (user_id, storage_source_id, root_path, enable, permissions)
select 2, storage_id, '/', instr(group_concat(operator), 'available') > 0, group_concat(operator) permissions from permission_config
where allow_anonymous = true
group by storage_id;