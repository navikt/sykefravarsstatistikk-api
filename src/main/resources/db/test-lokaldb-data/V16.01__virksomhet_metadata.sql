-- Enable auto-increment for H2 database
alter table virksomhet_metadata drop column id;
alter table virksomhet_metadata add id bigint auto_increment;

insert into virksomhet_metadata (orgnr, navn, rectype, sektor, naring_kode, arstall, kvartal) values ('999999999', 'Test bedrift AS', '2', '3','71', 2020, 3);
insert into virksomhet_metadata (orgnr, navn, rectype, sektor, naring_kode, arstall, kvartal) values ('987654321', 'En annen bedrift AS', '2', '3','71', 2020, 3);
