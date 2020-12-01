create table sykefravar_statistikk_virksomhet_gradering (
  id serial primary key,
  orgnr varchar(20) not null,
  naring varchar not null,
  naring_kode varchar not null,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null,
  antall numeric(17,0) not null, -- TODO: finne ut hva antall er
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,
  opprettet timestamp default current_timestamp
);
