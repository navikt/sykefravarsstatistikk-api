create table sykefravar_statistikk_naring_med_varighet (
  id serial primary key,
  naring_kode varchar not null,
  varighet varchar(1) not null,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,
  opprettet timestamp default current_timestamp
);
