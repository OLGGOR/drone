create table drone_medication
(
    drone_serial_number varchar(100) not null,
    medication_code     varchar(50)  not null,
    count               integer      not null,

    primary key (drone_serial_number, medication_code),

    constraint fk_medication foreign key (medication_code)
        references medications (code) on update restrict on delete restrict,
    constraint fk_drone foreign key (drone_serial_number)
        references drones (serial_number) on update restrict on delete restrict
);

commit;
