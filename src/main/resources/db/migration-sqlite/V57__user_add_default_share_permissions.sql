-- 为管理员默认补齐分享权限（createShareLink、customShareKey）

-- 补齐 user.default_permissions 中的 createShareLink
UPDATE user
SET default_permissions = CASE
    WHEN default_permissions IS NULL OR trim(coalesce(default_permissions, '')) = '' THEN 'createShareLink'
    WHEN instr(',' || replace(coalesce(default_permissions, ''), ' ', '') || ',', ',createShareLink,') = 0 THEN default_permissions || ',createShareLink'
    ELSE default_permissions
END
WHERE id = 1
  AND (
        default_permissions IS NULL
        OR trim(coalesce(default_permissions, '')) = ''
        OR instr(',' || replace(coalesce(default_permissions, ''), ' ', '') || ',', ',createShareLink,') = 0
    );

-- 补齐 user.default_permissions 中的 customShareKey
UPDATE user
SET default_permissions = CASE
    WHEN default_permissions IS NULL OR trim(coalesce(default_permissions, '')) = '' THEN 'customShareKey'
    WHEN instr(',' || replace(coalesce(default_permissions, ''), ' ', '') || ',', ',customShareKey,') = 0 THEN default_permissions || ',customShareKey'
    ELSE default_permissions
END
WHERE id = 1
  AND (
        default_permissions IS NULL
        OR trim(coalesce(default_permissions, '')) = ''
        OR instr(',' || replace(coalesce(default_permissions, ''), ' ', '') || ',', ',customShareKey,') = 0
    );

-- 补齐 user_storage_source.permissions 中的 createShareLink
UPDATE user_storage_source
SET permissions = CASE
    WHEN permissions IS NULL OR trim(coalesce(permissions, '')) = '' THEN 'createShareLink'
    WHEN instr(',' || replace(coalesce(permissions, ''), ' ', '') || ',', ',createShareLink,') = 0 THEN permissions || ',createShareLink'
    ELSE permissions
END
WHERE user_id = 1
  AND (
        permissions IS NULL
        OR trim(coalesce(permissions, '')) = ''
        OR instr(',' || replace(coalesce(permissions, ''), ' ', '') || ',', ',createShareLink,') = 0
    );

-- 补齐 user_storage_source.permissions 中的 customShareKey
UPDATE user_storage_source
SET permissions = CASE
    WHEN permissions IS NULL OR trim(coalesce(permissions, '')) = '' THEN 'customShareKey'
    WHEN instr(',' || replace(coalesce(permissions, ''), ' ', '') || ',', ',customShareKey,') = 0 THEN permissions || ',customShareKey'
    ELSE permissions
END
WHERE user_id = 1
  AND (
        permissions IS NULL
        OR trim(coalesce(permissions, '')) = ''
        OR instr(',' || replace(coalesce(permissions, ''), ' ', '') || ',', ',customShareKey,') = 0
    );
