create table medications
(
    code      varchar(50) primary key,
    name      varchar(50) not null,
    weight_gr numeric     not null check (weight_gr >= 1 and weight_gr <= 150),
    image     blob        not null
);

commit;
