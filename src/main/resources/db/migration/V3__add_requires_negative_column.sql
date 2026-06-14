ALTER TABLE operation_types
    ADD COLUMN requires_negative BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE operation_types SET requires_negative = TRUE
WHERE operation_type_id IN (1, 2, 3);

UPDATE operation_types SET requires_negative = FALSE
WHERE operation_type_id = 4