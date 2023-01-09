-- Aggregates statistikk tabeller fra datavarehus

-- Sykefravær
-- OBS: det er to rader per linje med stat (= arstall/kvartal/orgnr -sammen med sektor og næring- /alder/kjønn/fylkbo)
--  rad #1 har taptedv, mens muligedev og antpers er null
--  rad #2 har muligedev og antpers, mens taptedv er null (sftype er 'X' og varighet er 'X')

insert into dt_p.agg_ia_sykefravar_v
(arstall, kvartal,
 orgnr, naering_kode, sektor, storrelse, fylkarb,
 alder, kjonn, fylkbo,
 sftype, varighet,
 taptedv, muligedv, antpers, rectype)
values ('2019', '1',
        '998877661', '62', '3', 'G', '03',
        'B', 'K', '02',
        'L', 'A',
        103.123, 546.121, 88, '1');

insert into dt_p.agg_ia_sykefravar_v
(arstall, kvartal,
 orgnr, naering_kode, sektor, storrelse, fylkarb,
 alder, kjonn, fylkbo,
 sftype, varighet,
 taptedv, muligedv, antpers, rectype)
values ('2019', '1',
        '987654321', '62', '3', 'G', '03',
        'B', 'K', '02',
        'L', 'A',
        13.345, 44.111, 3, '2');

insert into dt_p.agg_ia_sykefravar_v
(arstall, kvartal,
 orgnr, naering_kode, sektor, storrelse, fylkarb,
 alder, kjonn, fylkbo,
 sftype, varighet,
 taptedv, muligedv, antpers, rectype)
values ('2019', '1',
        '987654321', '62', '3', 'G', '03',
        'B', 'K', '02',
        'X', 'X',
        34.876, 386.123, 6, '2');
insert into dt_p.agg_ia_sykefravar_v
(arstall, kvartal,
 orgnr, naering_kode, sektor, storrelse, fylkarb,
 alder, kjonn, fylkbo,
 sftype, varighet,
 taptedv, muligedv, antpers, rectype)
values ('2020', '1',
        '987654321', '62', '3', 'G', '03',
        'B', 'K', '02',
        'X', 'X',
        34.876, 386.123, 6, '2');
insert into dt_p.agg_ia_sykefravar_v
(arstall, kvartal,
 orgnr, naering_kode, sektor, storrelse, fylkarb,
 alder, kjonn, fylkbo,
 sftype, varighet,
 taptedv, muligedv, antpers, rectype)
values ('2022', '2',
        '987654321', '62', '3', 'G', '03',
        'B', 'K', '02',
        'X', 'X',
        34.876, 386.123, 6, '2');

