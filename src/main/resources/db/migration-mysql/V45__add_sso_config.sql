CREATE TABLE `sso_config`
(
    `provider`       varchar(255) NOT NULL PRIMARY KEY COMMENT '服务商',
    `issuer`         varchar(255) COMMENT '签发机构',
    `client_id`      varchar(255) NOT NULL COMMENT 'Client ID',
    `client_secret`  varchar(255) NOT NULL COMMENT 'Client Secret',
    `auth_url`       varchar(255) NOT NULL COMMENT '授权端点',
    `token_url`      varchar(255) NOT NULL COMMENT 'Token 端点',
    `user_info_url`  varchar(255) NOT NULL COMMENT '用户信息端点',
    `scope`          varchar(255) NOT NULL COMMENT '授权范围, 可填多个, 用空格隔开',
    `well_known_url` varchar(255) COMMENT '配置自动发现端点',
    `binding_field`  varchar(255) NOT NULL COMMENT '单点登录系统中用户与业务系统中用户的绑定字段',
    `enabled`        TINYINT(1)   NOT NULL COMMENT '服务商是否启用'
) DEFAULT CHARSET = utf8mb4
    COMMENT = '单点登录 (OIDC/OAuth2.0) 配置信息表';
