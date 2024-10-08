-- Tabeller fra datavarehus
create schema dvh_syfra;
create schema dk_p;

create table dvh_syfra.agg_ia_sykefravar_land_v
(
    arstall      char(4) not null,
    kvartal      char(1) not null,
    naring       char(2) not null,
    naringnavn   varchar(60),
    alder        char(1) not null,
    kjonn        char(1) not null,
    fylkbo       char(2) not null,
    fylknavn     varchar(35),
    varighet     char(1) not null,
    sektor       char(1) not null,
    sektornavn   varchar(60),
    taptedv      decimal(14, 6),
    muligedv     decimal(15, 6),
    antpers      decimal(7, 0),
    ia1_taptedv  decimal(14, 6),
    ia1_muligedv decimal(15, 6),
    ia1_antpers  decimal(7, 0),
    ia2_taptedv  decimal(14, 6),
    ia2_muligedv decimal(15, 6),
    ia2_antpers  decimal(7, 0),
    ia3_taptedv  decimal(14, 6),
    ia3_muligedv decimal(15, 6),
    ia3_antpers  decimal(7, 0)
);

create table dvh_syfra.v_agg_ia_sykefravar_naring
(
    arstall  char(4) not null,
    kvartal  char(1) not null,
    naring   char(2) not null,
    alder    char(1) not null,
    kjonn    char(1) not null,
    taptedv  decimal(14, 6),
    muligedv decimal(15, 6),
    antpers  decimal(7, 0)
);

create table dvh_syfra.agg_ia_sykefravar_v
(
    arstall      char(4)    not null,
    kvartal      char(1)    not null,
    orgnr        char(9)    not null,
    naering_kode char(5)    not null,
    alder        char(1)    not null,
    kjonn        char(1)    not null,
    fylkbo       varchar(2) not null,
    sftype       char(1)    not null,
    varighet     char(1)    not null,
    sektor       char(1)    not null,
    storrelse    char(1)    not null,
    fylkarb      varchar(2) not null,
    taptedv      decimal(14, 6),
    muligedv     decimal(15, 6),
    antpers      decimal(7, 0),
    rectype      char(1)    not null
);

create table dvh_syfra.agg_ia_sykefravar_naring_kode
(
    arstall      char(4) not null,
    kvartal      char(1) not null,
    naering_kode char(5) not null,
    alder        char(1) not null,
    kjonn        char(1) not null,
    taptedv      decimal(14, 6),
    muligedv     decimal(15, 6),
    antpers      decimal(7, 0)
);

create table dvh_syfra.agg_ia_sykefravar_v_2
(
    arstall           char(4),
    kvartal           char(1),
    orgnr             char(9) not null,
    sektor            char(1),
    naring            char(2),
    naering_kode      char(5),
    primærnæringskode char(6),
    alder             char(1),
    kjonn             char(1),
    fylkbo            varchar(2),
    kommnr            char(4),
    rectype           char(1),
    antall_gs         decimal(7, 0),
    taptedv_gs        decimal(14, 6),
    antall            decimal(7, 0),
    taptedv           decimal(14, 6),
    mulige_dv         decimal(15, 6),
    antpers           decimal(7, 0)
);


create table dk_p.publiseringstabell
(
    TABELL_NAVN        char(30),
    PERIODE_TYPE       char(20),
    RAPPORT_PERIODE    decimal(38),
    PREOFFENTLIG_DATO  DATE,
    PREOFFENTLIG_FLAGG decimal(1),
    OFFENTLIG_DATO     DATE,
    OFFENTLIG_FLAGG    decimal(1),
    AKTIVITET          char(120),
    LASTET_DATO        DATE,
    OPPDATERT_DATO     DATE,
    LASTET_AV          char(10),
    OPPDATERT_AV       char(10),
    KOMMENTAR          char(120)
)
