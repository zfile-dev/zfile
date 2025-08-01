INSERT INTO
    storage_source_config (name, type, title, storage_id)
SELECT
    'refreshTokenExpiredAt', type, '刷新令牌过期时间', storage_id
FROM
    storage_source_config
WHERE
    name = 'refreshToken';