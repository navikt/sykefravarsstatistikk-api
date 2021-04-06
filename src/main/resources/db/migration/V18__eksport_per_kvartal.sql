create table eksport_per_kvartal (
    id         serial primary key,
    orgnr      varchar  not null,
    arstall    smallint not null,
    kvartal    smallint not null,
    eksportert boolean   default false,
    opprettet  timestamp default current_timestamp
);