-- Enable auto-increment for H2 database
alter table eksport_per_kvartal drop column id;
alter table eksport_per_kvartal add id bigint auto_increment;

insert into eksport_per_kvartal (orgnr, arstall, kvartal) values ('999999999', 2020, 3);