
alter table sykefravar_statistikk_naring5siffer drop column id;
alter table sykefravar_statistikk_naring5siffer add id bigint auto_increment;


alter table virksomhet_metadata_naring_kode_5siffer drop column id;
alter table virksomhet_metadata_naring_kode_5siffer add id bigint auto_increment;