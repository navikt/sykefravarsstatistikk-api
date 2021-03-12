/*create table data_eksport_status (
    id serial primary key,
    arstall int not null,
    kvartal int not null,
    land_er_eksportert boolean default false ,
    sektor_er_eksportert boolean default false ,
    naring5siffer_er_eksportert boolean default false,
    naring_er_eksportert boolean default false ,
    naring_med_varighet boolean default false ,
    virksomhet_er_klar_for_sekportering boolean default false -- den er nå klar for sending til kafka

)
*/
alter table sykefravar_statistikk_virksomhet add column  er_ekportert boolean default false

--serial, arstall, kvartal, land, naring5,naring, naringmedvarighet,virk)
--(1111234, 2020, 4, false, false, false, false,false,true)

--(1111234, 2020, 4, false, false, false, false,false,false)

--(1111234, 2020, 4, false, false, true, false,false,true)
