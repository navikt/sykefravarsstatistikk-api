alter table sykefravar_statistikk_virksomhet add column varighet varchar(1);

DROP INDEX orgnr_arstall_kvartal__index;

CREATE INDEX orgnr_arstall_kvartalt__index
  ON sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal);

CREATE UNIQUE INDEX orgnr_arstall_kvartal_varighet__index
  ON sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, varighet);
