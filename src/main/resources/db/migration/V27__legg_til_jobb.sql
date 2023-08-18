ALTER TABLE import_eksport_status
    DROP COLUMN IF EXISTS importert_statistikk,
    DROP COLUMN IF EXISTS importert_virksomhetsdata,
    DROP COLUMN IF EXISTS forberedt_neste_eksport,
    DROP COLUMN IF EXISTS eksportert_paa_kafka;
ALTER TABLE import_eksport_status
    ADD COLUMN fullforte_jobber text NOT NULL default '';
