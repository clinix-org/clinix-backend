-- Apply once, before starting the new API. Stop the old API first.
-- This migration deliberately refuses to guess missing compositions for legacy data.
BEGIN;
LOCK TABLE public.medicamento IN ACCESS EXCLUSIVE MODE;
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM public.medicamento) THEN
        RAISE EXCEPTION 'Medication migration requires an empty legacy table. Preserve and map existing data before retrying.';
    END IF;
END $$;

ALTER TABLE public.medicamento RENAME TO medications;
ALTER TABLE public.medications RENAME COLUMN nome_comercial TO medication_name;
ALTER TABLE public.medications RENAME COLUMN categoria TO category;
ALTER TABLE public.medications RENAME COLUMN quantidade TO quantity;
ALTER TABLE public.medications DROP COLUMN nome_generico, DROP COLUMN unidade_medida;
ALTER TABLE public.medications
    ALTER COLUMN medication_name TYPE varchar(200),
    ALTER COLUMN medication_name SET NOT NULL,
    ALTER COLUMN category TYPE varchar(50),
    ALTER COLUMN category SET NOT NULL,
    ALTER COLUMN quantity SET NOT NULL,
    ALTER COLUMN quantity SET DEFAULT 0,
    ALTER COLUMN status TYPE varchar(30),
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'UNAVAILABLE',
    ADD COLUMN presentation_format varchar(50) NOT NULL,
    ADD COLUMN manufacturer varchar(200) NOT NULL,
    ADD COLUMN created_at timestamp with time zone NOT NULL,
    ADD COLUMN created_by varchar(255) NOT NULL,
    ADD COLUMN duplicate_key varchar(64) NOT NULL,
    ADD CONSTRAINT uk_medications_duplicate_key UNIQUE (duplicate_key),
    ADD CONSTRAINT ck_medications_quantity CHECK (quantity >= 0),
    ADD CONSTRAINT ck_medications_name CHECK (length(btrim(medication_name)) > 0),
    ADD CONSTRAINT ck_medications_status CHECK (status IN ('AVAILABLE', 'UNAVAILABLE'));
ALTER TABLE public.medications RENAME CONSTRAINT medicamento_pkey TO medications_pkey;
ALTER SEQUENCE public.medicamento_id_seq RENAME TO medications_id_seq;

CREATE TABLE public.medication_active_ingredients (
    medication_id bigint NOT NULL,
    ingredient_order integer NOT NULL,
    name varchar(200) NOT NULL,
    dosage numeric(18,6) NOT NULL,
    unit_measurement varchar(20) NOT NULL,
    CONSTRAINT pk_medication_active_ingredients PRIMARY KEY (medication_id, ingredient_order),
    CONSTRAINT fk_medication_active_ingredients FOREIGN KEY (medication_id)
        REFERENCES public.medications(id) ON DELETE CASCADE,
    CONSTRAINT ck_medication_ingredient_dosage CHECK (dosage > 0),
    CONSTRAINT ck_medication_ingredient_name CHECK (length(btrim(name)) > 0)
);
COMMIT;
