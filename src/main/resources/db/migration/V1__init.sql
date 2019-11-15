-- Dimensjon tabeller
create table sektor (
  kode varchar primary key,
  navn varchar(255) not null
);

create table naringsgruppe (
  kode varchar primary key,
  navn varchar(255) not null
);

create table naring (
  kode varchar primary key,
  naringsgruppe_kode varchar not null,
  navn varchar(255) not null,

  foreign key (naringsgruppe_kode) references naringsgruppe(kode)
);


-- Statistikk tabeller
create table sykefravar_statistikk_land (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null default 0,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table sykefravar_statistikk_sektor (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null default 0,
  sektor_kode varchar not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table sykefravar_statistikk_naring (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null default 0,
  naring_kode varchar not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,

  foreign key (naring_kode) references naring(kode)
);

create table sykefravar_statistikk_virksomhet (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  antall_personer numeric(17,0) not null default 0,
  orgnr varchar(20) not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);
