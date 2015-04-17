CREATE TABLE APP.TBL_RESULT (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    simulation_id integer not null,
    name varchar(250),
    value double,
    CONSTRAINT result_id PRIMARY KEY (id)
);

CREATE TABLE APP.TBL_SIMULATION (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    simulator_name varchar(250) not null,
    parameter_string varchar(1024) not null,
    output_file  varchar(32600),
    CONSTRAINT id PRIMARY KEY (id)
);
