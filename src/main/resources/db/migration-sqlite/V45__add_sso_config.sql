create table if not exists sso_config
(
    id             integer primary key autoincrement,
    provider       varchar(255) not null,
    name           varchar(255) not null,
    icon           text         not null,
    client_id      varchar(255) not null,
    client_secret  varchar(255) not null,
    auth_url       varchar(255) not null,
    token_url      varchar(255) not null,
    user_info_url  varchar(255) not null,
    scope          varchar(255) not null,
    binding_field  varchar(255) not null,
    enabled        bit      not null,
    order_num      int default 0 not null
);