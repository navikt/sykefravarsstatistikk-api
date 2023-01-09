create table publiseringsdatoer
(
    rapport_periode int     not null primary key,
    offentlig_dato  date    not null,
    oppdatert_i_dvh date    not null,
    aktivitet       varchar not null,
    importert       timestamp default current_timestamp
);

create table importtidspunkt
(
    aarstall  varchar not null,
    kvartal   varchar not null,
    importert timestamp not null
);
