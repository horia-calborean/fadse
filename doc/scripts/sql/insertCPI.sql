insert into fadse.tbl_result (
 select null, simulation_id, "Clocks per instruction CPI", 1 / value, now()
 from fadse.tbl_result where name = "instruction per clock cycle IPC" and (1 / value) > 0
)