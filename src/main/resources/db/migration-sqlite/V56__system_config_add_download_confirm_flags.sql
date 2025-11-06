INSERT INTO system_config (`name`, `title`, `value`)
SELECT 'enableNormalDownloadConfirm', '普通下载是否启用确认弹窗', 'true'
WHERE NOT EXISTS (
    SELECT 1 FROM system_config WHERE name = 'enableNormalDownloadConfirm'
);

INSERT INTO system_config (`name`, `title`, `value`)
SELECT 'enablePackageDownloadConfirm', '打包下载是否启用确认弹窗', 'true'
WHERE NOT EXISTS (
    SELECT 1 FROM system_config WHERE name = 'enablePackageDownloadConfirm'
);

INSERT INTO system_config (`name`, `title`, `value`)
SELECT 'enableBatchDownloadConfirm', '批量下载是否启用确认弹窗', 'true'
WHERE NOT EXISTS (
    SELECT 1 FROM system_config WHERE name = 'enableBatchDownloadConfirm'
);