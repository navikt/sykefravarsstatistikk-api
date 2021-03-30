create table virksomhet_eksportering_metadata (
  id serial primary key,
  orgnr varchar not null,
  arstall smallint not null,
  kvartal smallint not null,
  sektor varchar not null,
  naring_kode varchar not null,
  naring_kode_5siffer varchar not null,
  eksportert boolean default false,
  opprettet timestamp default current_timestamp
);
