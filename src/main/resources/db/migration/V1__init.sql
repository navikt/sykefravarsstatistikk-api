-- Dimensjonstabeller
create table sektor
(
    kode varchar primary key,
    navn varchar(255) not null
);

create table naring
(
    kode varchar primary key,
    navn varchar(255) not null
);


-- Statistikktabeller
create table sykefravar_statistikk_land
(
    id              serial primary key,
    arstall         smallint       not null,
    kvartal         smallint       not null,
    antall_personer numeric(17, 0) not null,
    tapte_dagsverk  numeric(17, 6) not null,
    mulige_dagsverk numeric(17, 6) not null,
    opprettet       timestamp default current_timestamp
);

create table sykefravar_statistikk_sektor
(
    id              serial primary key,
    sektor_kode     varchar        not null,
    arstall         smallint       not null,
    kvartal         smallint       not null,
    antall_personer numeric(17, 0) not null,
    tapte_dagsverk  numeric(17, 6) not null,
    mulige_dagsverk numeric(17, 6) not null,
    opprettet       timestamp default current_timestamp
);

create table sykefravar_statistikk_naring
(
    id              serial primary key,
    naring_kode     varchar        not null,
    arstall         smallint       not null,
    kvartal         smallint       not null,
    antall_personer numeric(17, 0) not null,
    tapte_dagsverk  numeric(17, 6) not null,
    mulige_dagsverk numeric(17, 6) not null,
    opprettet       timestamp default current_timestamp
);

create table sykefravar_statistikk_virksomhet
(
    id              serial primary key,
    orgnr           varchar(20)    not null,
    arstall         smallint       not null,
    kvartal         smallint       not null,
    antall_personer numeric(17, 0) not null,
    tapte_dagsverk  numeric(17, 6) not null,
    mulige_dagsverk numeric(17, 6) not null,
    opprettet       timestamp default current_timestamp
);
