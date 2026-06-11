-- folder_name is part of a department's identity but is nullable; a unique index treats NULLs as distinct, which
-- would defeat the constraint for folder-less departments. The service now stores "" instead of NULL, so normalize
-- existing NULL folder names to "" before adding the constraint.
update department set folder_name = '' where folder_name is null;

alter table department
    add constraint uq_department_batch_name_folder
        unique (batch_id, name, folder_name);
