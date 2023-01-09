create table virksomhet_metadata (
    id          serial primary key,
    orgnr       varchar not null,
    navn        varchar not null,
    rectype     varchar(1),
    sektor      varchar not null,
    naring_kode varchar not null,
    arstall    smallint not null,
    kvartal    smallint not null,
    opprettet   timestamp default current_timestamp
);

CREATE UNIQUE INDEX orgnr__virksomhet_metadata__index
  ON virksomhet_metadata (orgnr);
