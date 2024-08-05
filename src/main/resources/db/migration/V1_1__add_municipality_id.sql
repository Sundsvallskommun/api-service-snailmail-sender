alter table if exists batch
    add column municipality_id varchar(255);

create index idx_batch_municipality_id
    on batch (municipality_id);
