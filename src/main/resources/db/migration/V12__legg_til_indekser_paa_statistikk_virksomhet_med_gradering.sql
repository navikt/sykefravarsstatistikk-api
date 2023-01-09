--
CREATE INDEX orgnr_virksomhet_med_gradering__index
    ON sykefravar_statistikk_virksomhet_med_gradering (orgnr);

CREATE INDEX naring_virksomhet_med_gradering__index
    ON sykefravar_statistikk_virksomhet_med_gradering (naring);

CREATE INDEX naring_kode_virksomhet_med_gradering__index
    ON sykefravar_statistikk_virksomhet_med_gradering (naring_kode);
