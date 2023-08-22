ALTER TABLE import_eksport_status
DROP COLUMN importert_statistikk;

ALTER TABLE import_eksport_status
DROP COLUMN importert_virksomhetsdata;

ALTER TABLE import_eksport_status
DROP COLUMN forberedt_neste_eksport;

ALTER TABLE import_eksport_status
DROP COLUMN eksportert_paa_kafka;

ALTER TABLE import_eksport_status
ADD COLUMN fullforte_jobber text NOT NULL default '';