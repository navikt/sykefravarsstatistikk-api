-- Dimensjon tabeller fra datavarehus

-- Sektor
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('0','Ukjent');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('1','Statlig forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('2','Kommunal forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('3','Privat og offentlig næringsvirksomhet');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('9','Fylkeskommunal forvaltning');

-- Næring
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('11','C','Utvinning av råolje og naturgass. Tjenester tilknyttet olje- og gassutvinning');
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('14','B','Jordbruk, skogbruk og fiske');

-- Orgenhet
insert into dt_p.v_dim_ia_orgenhet (orgnr, offnavn, rectype, sektor, naring, arstall, kvartal)
values ('987654321','Test Virksomhet','2', '3', '89', 2020, 3);