create table publiseringsdatoer
(
    rapport_periode int     not null primary key,
    offentlig_dato  date    not null,
    oppdatert_i_dvh date    not null,
    aktivitet       varchar not null,
    er_publisert    boolean not null,
    importert       timestamp default current_timestamp
);

create table publiseringsstatus
(

)