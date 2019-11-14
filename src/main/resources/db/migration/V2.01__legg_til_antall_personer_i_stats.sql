-- Statistikk tabeller
alter table SYKEFRAVAR_STATISTIKK_LAND
    add column antall_personer numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_SEKTOR
    add column antall_personer numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_NARING
    add column antall_personer numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_VIRKSOMHET
    add column antall_personer numeric(17,0) not null default 0;

