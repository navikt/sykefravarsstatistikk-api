-- Aggregates statistikk tabeller fra datavarehus

-- Sykefravær
-- OBS: det er to rader per linje med stat (= arstall/kvartal/orgnr -sammen med sektor og næring- /alder/kjønn/fylkbo)
--  rad #1 har taptedv, mens muligedev og antpers er null
--  rad #2 har muligedev og antpers, mens taptedv er null (sftype er 'X' og varighet er 'X')

insert into dt_p.agg_ia_sykefravar_v
(
    arstall, kvartal,
    orgnr, naering_kode, sektor, storrelse, fylkarb,
    alder, kjonn,  fylkbo,
    sftype, varighet,
    taptedv, muligedv, antpers, rectype
)
values
(
    '2019','1',
    '987654321', '62', '3', 'G', '03',
    'B', 'K', '02',
    'L', 'A',
    13, 0, 0, '2'
);

insert into dt_p.agg_ia_sykefravar_v
(
    arstall, kvartal,
    orgnr, naering_kode, sektor, storrelse, fylkarb,
    alder, kjonn,  fylkbo,
    sftype, varighet,
    taptedv, muligedv, antpers, rectype
)
values
(
    '2019','1',
    '987654321', '62', '3', 'G', '03',
    'B', 'K', '02',
    'X', 'X',
    0, 386.123, 6, '2'
);