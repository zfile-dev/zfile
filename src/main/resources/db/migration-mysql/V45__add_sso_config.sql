CREATE TABLE IF NOT EXISTS `sso_config`
(
    id                  INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    `provider`       varchar(255) NOT NULL COMMENT '服务商简称',
    `name`           varchar(255) NOT NULL COMMENT '名称',
    `icon`           text         NOT NULL COMMENT '图标, 支持url和base64',
    `client_id`      varchar(255) NOT NULL COMMENT 'Client ID',
    `client_secret`  varchar(255) NOT NULL COMMENT 'Client Secret',
    `auth_url`       varchar(255) NOT NULL COMMENT '授权端点',
    `token_url`      varchar(255) NOT NULL COMMENT 'Token 端点',
    `user_info_url`  varchar(255) NOT NULL COMMENT '用户信息端点',
    `scope`          varchar(255) NOT NULL COMMENT '授权范围, 可填多个, 用空格隔开',
    `binding_field`  varchar(255) NOT NULL COMMENT '单点登录系统中用户与业务系统中用户的绑定字段',
    `enabled`        BIT          NOT NULL COMMENT '服务商是否启用',
    `order_num`      INT          NOT NULL COMMENT '排序字段, 越小越靠前' DEFAULT 0
) DEFAULT CHARSET = utf8mb4
    COMMENT = '单点登录 (OIDC/OAuth2.0) 配置信息表';