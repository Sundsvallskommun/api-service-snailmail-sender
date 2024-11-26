create table shedlock
(
    lock_until timestamp(3) not null,
    locked_at  timestamp(3) not null default current_timestamp(3),
    locked_by  varchar(255) not null,
    name       varchar(64)  not null,
    primary key (name)
);
