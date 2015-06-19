CREATE TABLE `cycle_monitor`.`boundary_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `threshold` FLOAT NULL,
  `upper` TINYINT NULL,
  PRIMARY KEY (`city`, `id`));
  
CREATE TABLE `cycle_monitor`.`rate_alert_config` (
  `city` VARCHAR(45) NOT NULL,
  `id` INT NOT NULL,
  `percent` FLOAT NULL,
  `duration` INT NULL,
  PRIMARY KEY (`city`, `id`));
  
