UPDATE system_config SET value = '' WHERE name = 'loginVerifySecret';
UPDATE system_config SET value = 'false' WHERE name = 'loginImgVerify';
UPDATE system_config SET value = 'false' WHERE name = 'adminTwoFactorVerify';