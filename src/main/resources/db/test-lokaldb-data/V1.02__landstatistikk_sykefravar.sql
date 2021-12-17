-- Enable auto-increment for H2 database
alter table sykefravar_statistikk_land drop column id;
alter table sykefravar_statistikk_land add id bigint auto_increment;

insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2014, 2, 10, 5884917.287819, 112525690.888394);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2014, 3, 10, 5880571.66224 , 107409308.353228);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2014, 4, 10, 6742042.862711, 124694740.804175);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2015, 1, 10, 6890524.230475, 127607137.939693);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2015, 2, 10, 5991969.563317, 122662885.907398);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2015, 3, 10, 5959018.859117, 138890110.098678);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2015, 4, 10, 6967356.669735, 137680452.98422);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2016, 1, 10, 6669831.320155, 127339213.439428);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2016, 2, 10, 6388935.367072, 131259560.164769);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2016, 3, 10, 6034238.967936, 142954052.457665);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2016, 4, 10, 7100497.771348, 138297804.424068);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2017, 1, 10, 7583139.563452, 138691199.722238);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2017, 2, 10, 6138915.399115, 125480463.239477);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2017, 3, 10, 6119874.096833, 143372091.640707);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2017, 4, 10, 7237496.092161, 138796863.077998);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2018, 1, 10, 7552683.649034, 136025318.382558);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2018, 2, 10, 6377350.632884, 133386098.230643);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2018, 3, 10, 6141222.614066, 145842198.17854);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2018, 4, 10, 7357660.879256, 143033580.877087);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2019, 1, 10, 7605099.154119, 138978910.743955);
insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values(2019, 2, 10, 6548546.846546, 145788910.465464);

commit