delete from tbl_simulation where id in (select simulation_id from tbl_result where value=1.79769e+308);
delete from tbl_result where value=1.79769e+308