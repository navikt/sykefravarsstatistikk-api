--
CREATE UNIQUE INDEX orgnr_arstall_kvartal__index
  ON sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal);
