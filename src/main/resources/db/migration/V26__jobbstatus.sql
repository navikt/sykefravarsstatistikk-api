create table import_eksport_status (
    aarstall varchar(4) not null,
    kvartal varchar(1) not null,
    importert_statistikk bool default false,
    importert_virksomhetsdata bool default false,
    forberedt_neste_eksport bool default false,
    eksportert_paa_kafka bool default false,
    created timestamp default current_timestamp,
    primary key (aarstall, kvartal)
);