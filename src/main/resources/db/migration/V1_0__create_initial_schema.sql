create table attachment
(
    id            bigint not null auto_increment,
    request_id    bigint,
    content_type  varchar(255),
    name          varchar(255),
    content       longtext,
    envelope_type enum ('WINDOWED','PLAIN'),
    primary key (id)
) engine = InnoDB;

create table batch
(
    id varchar(255) not null,
    primary key (id)
) engine = InnoDB;

create table department
(
    id       bigint not null auto_increment,
    batch_id varchar(255),
    name     varchar(255),
    primary key (id)
) engine = InnoDB;

create table recipient
(
    id          bigint not null auto_increment,
    address     varchar(255),
    care_of     varchar(255),
    city        varchar(255),
    given_name  varchar(255),
    last_name   varchar(255),
    postal_code varchar(255),
    primary key (id)
) engine = InnoDB;

create table request
(
    department_id bigint,
    id            bigint not null auto_increment,
    recipient_id  bigint,
    deviation     varchar(255),
    party_id      varchar(255),
    primary key (id)
) engine = InnoDB;

create index idx_department_name
    on department (name);

alter table if exists request
    add constraint UK_q3hqdcjoytjj5c5i5unhkqyjj unique (recipient_id);

alter table if exists attachment
    add constraint fk_attachment_request
        foreign key (request_id)
            references request (id);

alter table if exists department
    add constraint fk_department_batch
        foreign key (batch_id)
            references batch (id);

alter table if exists request
    add constraint fk_request_department
        foreign key (department_id)
            references department (id);

alter table if exists request
    add constraint fk_request_recipient
        foreign key (recipient_id)
            references recipient (id);
