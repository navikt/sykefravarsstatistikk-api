-- Dimensjon tabeller
create table SEKTOR (
  kode smallint primary key,
  navn varchar(255) not null
);

create table NARINGSGRUPPE (
  kode smallint primary key,
  navn varchar(255) not null
);

create table NARING (
  kode smallint primary key,
  naringsgruppe_kode smallint not null,
  navn varchar(255) not null,

  foreign key (naringsgruppe_kode) references NARINGSGRUPPE(kode)
);

create table VIRKSOMHET (
  orgnr varchar(20) primary key,
  sektor_kode smallint not null,
  naring_kode smallint not null,
  offnavn varchar(255) not null,

  foreign key (sektor_kode) references SEKTOR(kode),
  foreign key (naring_kode) references NARING(kode)
);


-- Statistikk tabeller
create table SYKEFRAVAR_STATISTIKK_LAND (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table SYKEFRAVAR_STATISTIKK_SEKTOR (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  sektor_kode smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null
);

create table SYKEFRAVAR_STATISTIKK_NARING (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  naring_kode smallint not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,

  foreign key (naring_kode) references NARING(kode)
);

create table SYKEFRAVAR_STATISTIKK_VIRKSOMHET (
  id serial primary key,
  opprettet timestamp not null default current_timestamp,
  arstall smallint not null,
  kvartal smallint not null,
  orgnr varchar(20) not null,
  tapte_dagsverk numeric(17,6) not null,
  mulige_dagsverk numeric(17,6) not null,

  foreign key (orgnr) references VIRKSOMHET(orgnr)
);
