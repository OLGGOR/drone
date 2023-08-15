create table drone_model
(
    model           varchar(30) primary key,
    weight_limit_gr numeric not null check (weight_limit_gr >= 10 and weight_limit_gr <= 500)
);

commit;
