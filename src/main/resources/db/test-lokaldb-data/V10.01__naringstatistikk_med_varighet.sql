-- Enable auto-increment for H2 database
alter table sykefravar_statistikk_naring_med_varighet drop column id;
alter table sykefravar_statistikk_naring_med_varighet add id bigint auto_increment;

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2019, 3, 10, 100.12443, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'B', 2019, 3, 10, 35.315251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2019, 3, 10, 85.535251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2019, 3, 10, 0, 929.320038);

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2019, 4, 10, 100.12443, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'B', 2019, 4, 10, 35.315251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2019, 4, 10, 85.535251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2019, 4, 10, 0, 929.320038);

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2020, 1, 10, 90.12443, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'C', 2020, 1, 10, 25.315251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'E', 2020, 1, 10, 11.544451, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2020, 1, 10, 65.535251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2020, 1, 10, 0, 1225.123456);

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2020, 2, 10, 33.18843, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'C', 2020, 2, 10, 65.39951, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'D', 2020, 2, 10, 15.456451, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'E', 2020, 2, 10, 1.544551, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2020, 2, 10, 55.445251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2020, 2, 10, 0, 999.123456);

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2020, 3, 10, 33.18843, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'C', 2020, 3, 10, 65.39951, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'D', 2020, 3, 10, 15.456451, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'E', 2020, 3, 10, 1.544551, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2020, 3, 10, 55.445251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2020, 3, 10, 0, 999.123456);

insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'A', 2020, 4, 10, 33.18843, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'C', 2020, 4, 10, 65.39951, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'D', 2020, 4, 10, 15.456451, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'E', 2020, 4, 10, 1.544551, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'F', 2020, 4, 10, 55.445251, 0);
insert into sykefravar_statistikk_naring_med_varighet (naring_kode, varighet, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values ('10', 'X', 2020, 4, 10, 0, 999.123456);

commit
