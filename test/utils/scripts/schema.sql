CREATE TABLE `boundary_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `upper` TINYINT NULL,
  `threshold` FLOAT NULL,
  PRIMARY KEY (`city`, `id`, `upper`));
  
CREATE TABLE `rate_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `percent` FLOAT NULL,
  `duration` INT NULL,
  PRIMARY KEY (`city`, `id`));
  
