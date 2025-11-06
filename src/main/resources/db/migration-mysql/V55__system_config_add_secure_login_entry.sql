INSERT INTO system_config (`name`, `title`)
SELECT 'secureLoginEntry', '安全登录入口'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE `name` = 'secureLoginEntry');