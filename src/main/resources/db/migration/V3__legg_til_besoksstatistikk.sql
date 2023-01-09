create table besoksstatistikk_virksomhet (
 id serial primary key,

 arstall smallint not null,
 kvartal smallint not null,
 sykefravarsprosent numeric(17,6),
 sykefravarsprosent_antall_personer smallint,
 naring_2siffer_sykefravarsprosent numeric(17,6) not null,
 ssb_sektor_sykefravarsprosent numeric(17,6) not null,

 orgnr varchar(20) not null,
 organisasjon_navn varchar not null,
 antall_ansatte smallint not null,
 naring_5siffer_kode varchar not null,
 naring_5siffer_beskrivelse varchar not null,
 naring_2siffer_beskrivelse varchar not null,
 institusjonell_sektor_kode varchar not null,
 institusjonell_sektor_beskrivelse varchar not null,
 ssb_sektor_kode varchar not null,
 ssb_sektor_beskrivelse varchar not null,

 session_id varchar,

 opprettet timestamp default current_timestamp
);

create table besoksstatistikk_smaa_virksomheter (
 id serial primary key,
 session_id varchar,
 opprettet timestamp default current_timestamp
);
