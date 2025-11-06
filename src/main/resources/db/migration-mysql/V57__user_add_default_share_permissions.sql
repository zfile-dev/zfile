-- 为管理员默认补齐分享权限（createShareLink、customShareKey）

-- 补齐 user.default_permissions 中的 createShareLink
UPDATE user
SET default_permissions = CASE
    WHEN default_permissions IS NULL OR default_permissions = '' THEN 'createShareLink'
    WHEN CONCAT(',', REPLACE(IFNULL(default_permissions, ''), ' ', ''), ',') NOT LIKE '%,createShareLink,%'
        THEN CONCAT_WS(',', NULLIF(default_permissions, ''), 'createShareLink')
    ELSE default_permissions
END
WHERE id = 1
  AND (
        default_permissions IS NULL
        OR default_permissions = ''
        OR CONCAT(',', REPLACE(IFNULL(default_permissions, ''), ' ', ''), ',') NOT LIKE '%,createShareLink,%'
    );

-- 补齐 user.default_permissions 中的 customShareKey
UPDATE user
SET default_permissions = CASE
    WHEN default_permissions IS NULL OR default_permissions = '' THEN 'customShareKey'
    WHEN CONCAT(',', REPLACE(IFNULL(default_permissions, ''), ' ', ''), ',') NOT LIKE '%,customShareKey,%'
        THEN CONCAT_WS(',', NULLIF(default_permissions, ''), 'customShareKey')
    ELSE default_permissions
END
WHERE id = 1
  AND (
        default_permissions IS NULL
        OR default_permissions = ''
        OR CONCAT(',', REPLACE(IFNULL(default_permissions, ''), ' ', ''), ',') NOT LIKE '%,customShareKey,%'
    );

-- 补齐 user_storage_source.permissions 中的 createShareLink
UPDATE user_storage_source
SET permissions = CASE
    WHEN permissions IS NULL OR permissions = '' THEN 'createShareLink'
    WHEN CONCAT(',', REPLACE(IFNULL(permissions, ''), ' ', ''), ',') NOT LIKE '%,createShareLink,%'
        THEN CONCAT_WS(',', NULLIF(permissions, ''), 'createShareLink')
    ELSE permissions
END
WHERE user_id = 1
  AND (
        permissions IS NULL
        OR permissions = ''
        OR CONCAT(',', REPLACE(IFNULL(permissions, ''), ' ', ''), ',') NOT LIKE '%,createShareLink,%'
    );

-- 补齐 user_storage_source.permissions 中的 customShareKey
UPDATE user_storage_source
SET permissions = CASE
    WHEN permissions IS NULL OR permissions = '' THEN 'customShareKey'
    WHEN CONCAT(',', REPLACE(IFNULL(permissions, ''), ' ', ''), ',') NOT LIKE '%,customShareKey,%'
        THEN CONCAT_WS(',', NULLIF(permissions, ''), 'customShareKey')
    ELSE permissions
END
WHERE user_id = 1
  AND (
        permissions IS NULL
        OR permissions = ''
        OR CONCAT(',', REPLACE(IFNULL(permissions, ''), ' ', ''), ',') NOT LIKE '%,customShareKey,%'
    );
