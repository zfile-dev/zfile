CREATE TABLE IF NOT EXISTS `share_link`
(
    `id`             INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    `share_key`      varchar(255) NOT NULL COMMENT '分享链接 key',
    `password`       varchar(8)   NULL COMMENT '分享密码',
    `expire_date`    datetime     NULL COMMENT '过期时间',
    `storage_key`    varchar(255) NOT NULL COMMENT '存储源key',
    `share_path`     text         NOT NULL COMMENT '分享所在目录',
    `share_item`     text         NOT NULL COMMENT '分享项目(JSON格式)',
    `create_date`    datetime     NOT NULL COMMENT '创建时间',
    `share_type`     varchar(20)  NOT NULL COMMENT '分享类型: FILE/FOLDER/MULTIPLE',
    `user_id`        INT          NOT NULL COMMENT '创建分享的用户ID',
    `download_count` INT          NOT NULL DEFAULT 0 COMMENT '下载次数',
    `access_count`   INT          NOT NULL DEFAULT 0 COMMENT '访问次数',
    UNIQUE KEY `idx_share_key` (`share_key`)
) DEFAULT CHARSET = utf8mb4
    COMMENT = '文件分享链接表';