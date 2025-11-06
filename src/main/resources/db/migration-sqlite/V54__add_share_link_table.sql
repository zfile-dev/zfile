create table share_link
(
    id          integer primary key autoincrement,
    share_key   varchar(255),  -- 分享链接 key
    password    varchar(8),    -- 分享密码
    expire_date datetime,      -- 过期时间
    storage_key varchar(255),  -- 存储源key
    share_path  text,          -- 分享所在目录
    share_item  text,          -- 分享项目(JSON格式)
    create_date datetime,      -- 创建时间
    share_type     varchar(20),-- 分享类型: FILE/FOLDER/MULTIPLE
    user_id        integer,    -- 创建分享的用户ID
    download_count integer default 0, -- 下载次数
    access_count   integer default 0 -- 访问次数
);

create unique index idx_share_key on share_link(share_key);