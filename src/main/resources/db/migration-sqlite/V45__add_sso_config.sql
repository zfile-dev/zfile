create table if not exists sso_config
(
    provider       varchar(255) not null
        constraint sso_config_pk primary key,
    name           varchar(255) not null,
    icon           text         not null ,
    client_id      varchar(255) not null,
    client_secret  varchar(255) not null,
    auth_url       varchar(255) not null,
    token_url      varchar(255) not null,
    user_info_url  varchar(255) not null,
    scope          varchar(255) not null,
    binding_field  varchar(255) not null,
    enabled        bit      not null
);
