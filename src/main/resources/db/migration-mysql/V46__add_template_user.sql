INSERT INTO `user` (username, nickname, enable, create_time, update_time) VALUES ( 'template', '虚拟新用户', true, now(), now());
UPDATE `user` SET `id` = 0 WHERE `username` = 'template';