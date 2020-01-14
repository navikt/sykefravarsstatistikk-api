-- Aggregates statistikk tabeller fra datavarehus

-- Næring
-- OBS: det er to rader per linje med stat (= arstall/kvartal/næring/alder/kjønn)
--  rad #1 har muligedev og antpers, mens taptedv er null
--  rad #2 har taptedv, mens muligedev og antpers er null
insert into dt_p.v_agg_ia_sykefravar_naring
 (
  arstall, kvartal,
  naring,
  alder, kjonn,
  taptedv, muligedv, antpers
 )
 values
 (
  '2019','1',
  '23',
  'A', 'K',
  0, 2649.968008, 105
 );
insert into dt_p.v_agg_ia_sykefravar_naring
(
    arstall, kvartal,
    naring,
    alder, kjonn,
    taptedv, muligedv, antpers
)
values
(
    '2019','1',
    '23',
    'A', 'K',
    29.184758, 0, 0
);

insert into dt_p.v_agg_ia_sykefravar_naring
(
    arstall, kvartal,
    naring,
    alder, kjonn,
    taptedv, muligedv, antpers
)
values
(
    '2019','1',
    '23',
    'A', 'M',
    0, 12037.443284, 254
);
insert into dt_p.v_agg_ia_sykefravar_naring
(
    arstall, kvartal,
    naring,
    alder, kjonn,
    taptedv, muligedv, antpers
)
values
(
    '2019','1',
    '23',
    'A', 'M',
    332.240185, 0, 0
);

