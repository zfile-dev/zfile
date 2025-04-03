INSERT INTO system_config (`name`, `title`, `value`)
SELECT 'loginImgVerify', '是否启用登录图片验证码',
       IF(
                   (SELECT value FROM system_config WHERE name = 'loginVerifyMode') = 'image',
                   'true',
                   'false'
           );

INSERT INTO system_config (`name`, `title`, `value`)
SELECT 'adminTwoFactorVerify', '是否为管理员启用双因素认证',
       IF(
                   (SELECT value FROM system_config WHERE name = 'loginVerifyMode') = '2fa',
                   'true',
                   'false'
           );