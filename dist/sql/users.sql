CREATE TABLE IF NOT EXISTS `users` (
  `id` INT(10) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `level` INT(1) unsigned NOT NULL DEFAULT 0,
  `order` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`order`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;