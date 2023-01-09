create table virksomhet_metadata_naring_kode_5siffer
(
    id                  serial primary key,
    orgnr               varchar  not null,
    naring_kode         varchar  not null,
    naring_kode_5siffer varchar  not null,
    arstall             smallint not null,
    kvartal             smallint not null,
    opprettet           timestamp default current_timestamp
);

CREATE UNIQUE INDEX orgnr_naring_kode_5siffer__virksomhet_metadata__index
    ON virksomhet_metadata_naring_kode_5siffer (orgnr, naring_kode, naring_kode_5siffer);
