-- Dimensjon tabeller fra datavarehus

-- Sektor
insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE,SEKTORNAVN) values ('0','Ukjent');
insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE,SEKTORNAVN) values ('1','Statlig forvaltning');
insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE,SEKTORNAVN) values ('2','Kommunal forvaltning');
insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE,SEKTORNAVN) values ('3','Privat og offentlig næringsvirksomhet');
insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE,SEKTORNAVN) values ('9','Fylkeskommunal forvaltning');

-- Gruppert næring
INSERT INTO dt_p.V_DIM_IA_FGRP_NARING_SN2007 (NARGRPKODE,NARGRPNAVN) VALUES ('C','Industri og bergverksdrift');
INSERT INTO dt_p.V_DIM_IA_FGRP_NARING_SN2007 (NARGRPKODE,NARGRPNAVN) VALUES ('B','Film');

-- Næring
INSERT INTO dt_p.V_DIM_IA_NARING_SN2007 (NARINGKODE,NARGRPKODE,NARINGNAVN)
 VALUES ('11','C','Utvinning av råolje og naturgass. Tjenester tilknyttet olje- og gassutvinning');
INSERT INTO dt_p.V_DIM_IA_NARING_SN2007 (NARINGKODE,NARGRPKODE,NARINGNAVN)
 VALUES ('14','B','Jordbruk, skogbruk og fiske');
