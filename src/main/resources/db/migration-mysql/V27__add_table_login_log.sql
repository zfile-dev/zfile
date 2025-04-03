create table if not exists login_log
(
    id   int auto_increment primary key,
    username varchar(255) null comment '用户名',
    nickname varchar(255) null comment '用户昵称',
    password varchar(255) null comment '密码',
    create_time datetime null comment '登录时间',
    ip varchar(20) null comment '登录 ip',
    user_agent varchar(2048) null comment '登录 user_agent',
    referer varchar(2048) null comment '登录 referer',
    result varchar(255) null comment '登录结果'
)
    comment '登录日志';