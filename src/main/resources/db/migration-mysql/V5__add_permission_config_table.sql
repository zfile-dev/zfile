create table if not exists permission_config
(
    id              int auto_increment primary key ,
    operator        varchar(32) null comment '操作',
    allow_admin     bit         null comment '允许管理员操作',
    allow_anonymous bit         null comment '允许匿名用户操作',
    storage_id      int         null comment '存储源 ID'
)
    comment '权限设置表';