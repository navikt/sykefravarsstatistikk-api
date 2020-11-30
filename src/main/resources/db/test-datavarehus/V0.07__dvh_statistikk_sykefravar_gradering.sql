-- Gradering
insert into dt_p.agg_ia_sykefravar_v_2
 (
  arstall, kvartal,
  orgnr, naering_kode, naring,
  alder, kjonn, fylkbo, kommnr,
  rectype,
  antall_gs, taptedv_gs,
  antall,
  taptedv, muligedv, antpers
 )
 values
 (
  '2020','1',
  '987654321', '01110', '01',
  'C', 'K', '42', '4215',
  '1',
  1, 2.730422,
  7,
  78.048722, 1269.433466, 42
 );

