create table if not exists login_log
(
    id                            integer
        primary key autoincrement,
    username varchar(255) null,
    password varchar(255) null,
    create_time datetime null,
    ip varchar(64) null,
    user_agent varchar(2048) null,
    referer varchar(2048) null,
    result varchar(255) null
);