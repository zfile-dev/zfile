create table if not exists permission_config
(
    id                            integer
        primary key autoincrement,
    operator        varchar(32) null,
    allow_admin     bit         null,
    allow_anonymous bit         null,
    storage_id      int         null
);