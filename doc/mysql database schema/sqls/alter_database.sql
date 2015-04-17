ALTER TABLE `tbl_simulation` 
 ADD COLUMN `parameter_string_hash` VARCHAR(32) NULL AFTER `parameter_string` , 
 ADD INDEX `par_hash_index` (`parameter_string_hash` ASC);
 
update tbl_simulation set parameter_string_hash = md5(parameter_string);
update tbl_simulation set output_file = compress(output_file);