INSERT INTO system_config (`name`, `title`, `value`)
select 'mobileFileClickMode', '移动端默认文件点击模式', value
from system_config
where name = 'fileClickMode';