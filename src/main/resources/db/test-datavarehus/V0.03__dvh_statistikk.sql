-- Aggregates statistikk tabeller fra datavarehus

-- Land
insert into dt_p.v_agg_ia_sykefravar_land
 (
  arstall, kvartal,
  naring, naringnavn,
  alder, kjonn,
  fylkbo, fylknavn,
  varighet, sektor, sektornavn,
  taptedv, muligedv, antpers,
  ia1_taptedv, ia1_muligedv, ia1_antpers,
  ia2_taptedv, ia2_muligedv, ia2_antpers,
  ia3_taptedv, ia3_muligedv, ia3_antpers
 )
 values
 (
  '2019','1',
  '41', 'Bygge- og anleggsvirksomhet',
  'A', 'M',
  '06', 'Buskerud',
  'A', '1', 'Statlig forvaltning',
  9, 0, 0,
  0, 0, 0,
  0, 0, 0,
  0, 0, 0
 );
insert into dt_p.v_agg_ia_sykefravar_land
(
    arstall, kvartal,
    naring, naringnavn,
    alder, kjonn,
    fylkbo, fylknavn,
    varighet, sektor, sektornavn,
    taptedv, muligedv, antpers,
    ia1_taptedv, ia1_muligedv, ia1_antpers,
    ia2_taptedv, ia2_muligedv, ia2_antpers,
    ia3_taptedv, ia3_muligedv, ia3_antpers
)
values
(
    '2019','1',
    '41', 'Bygge- og anleggsvirksomhet',
    'F', 'M',
    '06', 'Buskerud',
    'X', '1', 'Statlig forvaltning',
    0, 2206.6, 37,
    0, 0, 0,
    0, 0, 0,
    0, 0, 0
);
insert into dt_p.v_agg_ia_sykefravar_land
(
    arstall, kvartal,
    naring, naringnavn,
    alder, kjonn,
    fylkbo, fylknavn,
    varighet, sektor, sektornavn,
    taptedv, muligedv, antpers,
    ia1_taptedv, ia1_muligedv, ia1_antpers,
    ia2_taptedv, ia2_muligedv, ia2_antpers,
    ia3_taptedv, ia3_muligedv, ia3_antpers
)
values
(
    '2019','1',
    '41', 'Bygge- og anleggsvirksomhet',
    'D', 'M',
    '06', 'Buskerud',
    'B', '1', 'Statlig forvaltning',
    10, 0, 0,
    0, 0, 0,
    0, 0, 0,
    0, 0, 0
);
insert into dt_p.v_agg_ia_sykefravar_land
(
    arstall, kvartal,
    naring, naringnavn,
    alder, kjonn,
    fylkbo, fylknavn,
    varighet, sektor, sektornavn,
    taptedv, muligedv, antpers,
    ia1_taptedv, ia1_muligedv, ia1_antpers,
    ia2_taptedv, ia2_muligedv, ia2_antpers,
    ia3_taptedv, ia3_muligedv, ia3_antpers
)
values
(
    '2019','1',
    '41', 'Bygge- og anleggsvirksomhet',
    'D', 'M',
    '06', 'Buskerud',
    'C', '1', 'Statlig forvaltning',
    38, 0, 0,
    0, 0, 0,
    0, 0, 0,
    0, 0, 0
);
