-- Tabeller fra datavarehus
create schema dt_p;

create table dt_p.V_DIM_IA_SEKTOR
(
    sektorkode char(1),
    sektornavn varchar(60)
);

create table dt_p.V_DIM_IA_FGRP_NARING_SN2007
(
    nargrpkode char(2) not null,
    nargrpnavn varchar(60) not null,
    primary key (nargrpkode)
);

create table dt_p.V_DIM_IA_NARING_SN2007
(
    naringkode char(2) not null,
    nargrpkode char(2) not null,
    naringnavn varchar(100) not null,
    constraint r1_naring_sn2007_pk primary key (naringkode)
);

create table dt_p.V_AGG_IA_SYKEFRAVAR_LAND
(
     arstall char(4) not null,
     kvartal char(1) not null,
     naring char(2) not null,
     naringnavn varchar(60),
     alder char(1) not null,
     kjonn char(1) not null,
     fylkbo char(2) not null,
     fylknavn varchar(35),
     varighet char(1) not null,
     sektor char(1) not null,
     sektornavn varchar(60),
     taptedv decimal(14,6),
     muligedv decimal(15,6),
     antpers decimal(7,0),
     ia1_taptedv decimal(14,6),
     ia1_muligedv decimal(15,6),
     ia1_antpers decimal(7,0),
     ia2_taptedv decimal(14,6),
     ia2_muligedv decimal(15,6),
     ia2_antpers decimal(7,0),
     ia3_taptedv decimal(14,6),
     ia3_muligedv decimal(15,6),
     ia3_antpers decimal(7,0)
)