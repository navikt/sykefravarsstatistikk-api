-- Dimensjon tabeller fra datavarehus

-- Sektor
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('0','Ukjent');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('1','Statlig forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('2','Kommunal forvaltning');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('3','Privat og offentlig næringsvirksomhet');
insert into dt_p.v_dim_ia_sektor (sektorkode,sektornavn) values ('9','Fylkeskommunal forvaltning');

-- Næring 2 siffer
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('11','C','Utvinning av råolje og naturgass. Tjenester tilknyttet olje- og gassutvinning');
insert into dt_p.v_dim_ia_naring_sn2007 (naringkode,nargrpkode,naringnavn)
 values ('14','B','Jordbruk, skogbruk og fiske');

-- Næring 5 siffer
insert into dt_p.dim_ia_naring (naering_kode, naering_besk_land, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang)
values ('33160','Reparasjon og vedlikehold av luftfartøyer og romfartøyer','3316','Reparasjon og vedlikehold av luftfartøyer og romfartøyer','331','Reparasjon av metallvarer, maskiner og utstyr','33','Reparasjon og installasjon av maskiner og utstyr','10','Industri');
insert into dt_p.dim_ia_naring (naering_kode, naering_besk_land, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang)
values ('33190', 'Reparasjon av annet utstyr','3319','Reparasjon av annet utstyr','331','Reparasjon av metallvarer, maskiner og utstyr','33','Reparasjon og installasjon av maskiner og utstyr','10','Industri');
insert into dt_p.dim_ia_naring (naering_kode, naering_besk_land, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang)
values ('33200','Installasjon av industrimaskiner og -utstyr','3320','Installasjon av industrimaskiner og -utstyr','332','Installasjon av industrimaskiner og -utstyr','33','Reparasjon og installasjon av maskiner og utstyr','10','Industri');
insert into dt_p.dim_ia_naring (naering_kode, naering_besk_land, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang)
values ('35111','Produksjon av elektrisitet fra vannkraft','3511','Produksjon av elektrisitet','351','Produksjon, overføring og distribusjon av elektrisitet','35','Elektrisitets-, gass-, damp- og varmtvannsforsyning','35','Elektrisitet-, vann og renovasjon');
