create table besoksstatistikk_virksomhet (
 id serial primary key,
 sykefravarsprosent numeric(17,0) not null,
 antall_ansatte smallint not null,
 orgnr varchar(20) not null,
 naring_kode varchar not null,
 sektor_kode varchar not null,
 arstall smallint not null,
 kvartal smallint not null,
 cookie varchar,
 opprettet timestamp default current_timestamp
);

create table besoksstatistikk_smaa_virksomheter (
 id serial primary key,
 antall_smaa_virksomheter smallint not null,
 opprettet timestamp default current_timestamp
);