CREATE TABLE IF NOT EXISTS user
(
    id                  INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username            VARCHAR(255) NULL COMMENT '用户名',
    nickname            varchar(255) NULL COMMENT '昵称',
    password            VARCHAR(32)  NULL COMMENT '用户密码',
    enable              BIT          NULL COMMENT '是否启用',
    create_time         DATETIME     NULL COMMENT '创建时间',
    update_time         DATETIME     NULL COMMENT '更新时间',
    default_permissions TEXT         NULL COMMENT '默认权限'
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS user_storage_source
(
    id                INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户存储源ID',
    user_id           INT  NULL COMMENT '用户ID',
    storage_source_id INT  NULL COMMENT '存储源ID',
    root_path         TEXT NULL COMMENT '根路径',
    enable            BIT  NULL COMMENT '是否启用',
    permissions        TEXT NULL COMMENT '权限列表'
) COMMENT '用户存储源表';

insert into user (id, username, nickname, password, enable, create_time) values (1, (select value from system_config where name = 'username'), '管理员', (select value from system_config where name = 'password'), true, current_timestamp);
insert into user (id, username, nickname, password, enable, create_time) values (2, 'guest', '匿名用户', null, true, current_timestamp);

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