--
CREATE INDEX orgnr_virksomhet_for_gradert_sykemelding__index
  ON sykefravar_statistikk_virksomhet_for_gradert_sykemelding (orgnr);

CREATE INDEX naring_virksomhet_for_gradert_sykemelding__index
  ON sykefravar_statistikk_virksomhet_for_gradert_sykemelding (naring);

CREATE INDEX naring_kode_virksomhet_for_gradert_sykemelding__index
  ON sykefravar_statistikk_virksomhet_for_gradert_sykemelding (naring_kode);