alter table batch
  rename column issuer to sent_by;
alter table batch
  modify municipality_id varchar(4);
