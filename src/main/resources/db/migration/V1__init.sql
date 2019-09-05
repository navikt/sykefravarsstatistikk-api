-- Dimensjon tabeller
create table SEKTOR (
  id serial primary key,
  kode smallint not null,
  navn varchar(255) not null,
);

create table NARINGSGRUPPE (
  id serial primary key,
  kode smallint not null,
  navn varchar(255) not null
);

create table NARING (
  id serial primary key,
  kode smallint not null,
  naringsgruppe_kode smallint not null,
  navn varchar(255) not null,

  foreign key (naringsgruppe_kode) references NARINGSGRUPPE(kode)
);

create table VIRKSOMHET (
  id serial primary key,
  orgnr varchar(20) not null,
  sektor_kode smallint not null,
  naring_kode smallint not null,
  offnavn varchar(255) not null,

  foreign key (sektor_kode) references SEKTOR(kode),
  foreign key (naring_kode) references NARING(kode)
);


-- Statistikk tabeller
create table LAND_STATISTIKK_SYKEFRAVAR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table SEKTOR_STATISTIKK_SYKEFRAVAR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  sektor_kode smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table NARING_STATISTIKK_SYKEFRAVAR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  naring_kode smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,

  foreign key (naring_kode) references NARING(kode)
);

create table VIRKSOMHET_STATISTIKK_SYKEFRAVAR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  orgnr varchar(20) not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,

  foreign key (orgnr) references VIRKSOMHET(orgnr)
);
