create table besoksstatistikk_unikt_besok (
 id serial primary key,
 ar smallint not null,
 uke smallint not null,
);

create table besoksstatistikk_altinn_roller (
 id serial primary key,
 unikt_besok_id number,
 altinn_rolle varchar
);
