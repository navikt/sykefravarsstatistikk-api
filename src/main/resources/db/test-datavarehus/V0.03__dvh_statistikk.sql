-- Aggregates statistikk tabeller fra datavarehus

-- Land
INSERT INTO dt_p.V_AGG_IA_SYKEFRAVAR_LAND
 (
  ARSTALL, KVARTAL,
  NARING, NARINGNAVN,
  ALDER, KJONN,
  FYLKBO, FYLKNAVN,
  VARIGHET, SEKTOR, SEKTORNAVN,
  TAPTEDV, MULIGEDV, ANTPERS,
  IA1_TAPTEDV, IA1_MULIGEDV, IA1_ANTPERS,
  IA2_TAPTEDV, IA2_MULIGEDV, IA2_ANTPERS,
  IA3_TAPTEDV, IA3_MULIGEDV, IA3_ANTPERS
 )
 VALUES
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
INSERT INTO dt_p.V_AGG_IA_SYKEFRAVAR_LAND
(
    ARSTALL, KVARTAL,
    NARING, NARINGNAVN,
    ALDER, KJONN,
    FYLKBO, FYLKNAVN,
    VARIGHET, SEKTOR, SEKTORNAVN,
    TAPTEDV, MULIGEDV, ANTPERS,
    IA1_TAPTEDV, IA1_MULIGEDV, IA1_ANTPERS,
    IA2_TAPTEDV, IA2_MULIGEDV, IA2_ANTPERS,
    IA3_TAPTEDV, IA3_MULIGEDV, IA3_ANTPERS
)
VALUES
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
INSERT INTO dt_p.V_AGG_IA_SYKEFRAVAR_LAND
(
    ARSTALL, KVARTAL,
    NARING, NARINGNAVN,
    ALDER, KJONN,
    FYLKBO, FYLKNAVN,
    VARIGHET, SEKTOR, SEKTORNAVN,
    TAPTEDV, MULIGEDV, ANTPERS,
    IA1_TAPTEDV, IA1_MULIGEDV, IA1_ANTPERS,
    IA2_TAPTEDV, IA2_MULIGEDV, IA2_ANTPERS,
    IA3_TAPTEDV, IA3_MULIGEDV, IA3_ANTPERS
)
VALUES
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
INSERT INTO dt_p.V_AGG_IA_SYKEFRAVAR_LAND
(
    ARSTALL, KVARTAL,
    NARING, NARINGNAVN,
    ALDER, KJONN,
    FYLKBO, FYLKNAVN,
    VARIGHET, SEKTOR, SEKTORNAVN,
    TAPTEDV, MULIGEDV, ANTPERS,
    IA1_TAPTEDV, IA1_MULIGEDV, IA1_ANTPERS,
    IA2_TAPTEDV, IA2_MULIGEDV, IA2_ANTPERS,
    IA3_TAPTEDV, IA3_MULIGEDV, IA3_ANTPERS
)
VALUES
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
