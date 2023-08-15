create table drones
(
    serial_number   varchar(100) primary key,
    model           varchar(30) not null,
    battery_level   numeric     not null check (battery_level >= 0 and battery_level <= 100),
    state           varchar(30) not null,

    constraint fk_model foreign key (model)
        references drone_model (model) on update restrict on delete restrict
);

commit;
