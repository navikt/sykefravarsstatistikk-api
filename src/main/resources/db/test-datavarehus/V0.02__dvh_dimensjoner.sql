-- Dimensjon tabeller fra datavarehus

-- Sektor
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('0','Ukjent');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('1','Statlig forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('2','Kommunal forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('3','Privat og offentlig næringsvirksomhet');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('9','Fylkeskommunal forvaltning');

-- Gruppert næring
insert into dt_p.v_dim_ia_fgrp_naring_sn2007 (nargrpkode,nargrpnavn) values ('C','Industri og bergverksdrift');
insert into dt_p.v_dim_ia_fgrp_naring_sn2007 (nargrpkode,nargrpnavn) values ('B','Film');

-- Næring
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('11','C','Utvinning av råolje og naturgass. Tjenester tilknyttet olje- og gassutvinning');
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('14','B','Jordbruk, skogbruk og fiske');
