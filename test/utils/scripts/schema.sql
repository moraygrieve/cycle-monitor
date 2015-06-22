CREATE TABLE `cycle_monitor`.`boundary_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `upper` TINYINT NULL,
  `threshold` FLOAT NULL,
  PRIMARY KEY (`city`, `id`, `upper`));
  
CREATE TABLE `cycle_monitor`.`rate_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `percent` FLOAT NULL,
  `duration` INT NULL,
  PRIMARY KEY (`city`, `id`));
  
