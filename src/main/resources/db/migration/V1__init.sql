create table LAND_STATISTIKK_SYKEFRAVAR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);
