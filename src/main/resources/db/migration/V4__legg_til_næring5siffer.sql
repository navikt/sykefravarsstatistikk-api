create table sykefravar_statistikk_naring5siffer (
  id serial primary key,
  naring_kode varchar not null,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,
  opprettet timestamp default current_timestamp
);

create table naringsgruppering (
  kode_5siffer varchar(5) primary key,
  beskrivelse_5siffer varchar(255) not null,
  kode_4siffer varchar(4) not null,
  beskrivelse_4siffer varchar(255) not null,
  kode_3siffer varchar(3) not null,
  beskrivelse_3siffer varchar(255) not null,
  kode_2siffer varchar(2) not null,
  beskrivelse_2siffer varchar(255) not null,
  kode_hovedomrade varchar not null,
  beskrivelse_hovedomrade varchar(255) not null,
);