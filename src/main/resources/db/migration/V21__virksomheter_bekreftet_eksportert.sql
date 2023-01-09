create table virksomheter_bekreftet_eksportert
(
    orgnr     varchar  not null primary key,
    arstall   smallint not null,
    kvartal   smallint not null,
    opprettet timestamp default current_timestamp
);
