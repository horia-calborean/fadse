# --------------------------------------------------------
# Host:                         172.16.21.45
# Database:                     fadse_db
# Server version:               5.1.49-community
# Server OS:                    Win32
# HeidiSQL version:             5.0.0.3272
# Date/time:                    2010-08-30 10:54:41
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
# Dumping database structure for fadse_db
CREATE DATABASE IF NOT EXISTS `fadse_db` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `fadse_db`;


# Dumping structure for table fadse_db.tbl_result
CREATE TABLE IF NOT EXISTS `tbl_result` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `simulation_id` int(10) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `value` double DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `TBL_RESULT_FKIndex1` (`simulation_id`),
  CONSTRAINT `tbl_result_ibfk_1` FOREIGN KEY (`simulation_id`) REFERENCES `tbl_simulation` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table fadse_db.tbl_simulation
CREATE TABLE IF NOT EXISTS `tbl_simulation` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `simulator_name` varchar(255) NOT NULL,
  `parameter_string` text,
  `output_file` text,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
