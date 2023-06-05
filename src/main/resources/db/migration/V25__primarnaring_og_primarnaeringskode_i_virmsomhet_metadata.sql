ALTER TABLE virksomhet_metadata
ADD COLUMN primarnaringskode varchar(5);

ALTER TABLE virksomhet_metadata
RENAME COLUMN naring_kode TO primarnaring;
