create table sso_config
(
    provider       varchar(255) not null
        constraint sso_config_pk primary key,
    issuer         varchar(255),
    client_id      varchar(255) not null,
    client_secret  varchar(255) not null,
    auth_url       varchar(255) not null,
    token_url      varchar(255) not null,
    user_info_url  varchar(255) not null,
    scope          varchar(255) not null,
    well_known_url varchar(255),
    binding_field  varchar(255) not null,
    enabled        integer      not null
);
