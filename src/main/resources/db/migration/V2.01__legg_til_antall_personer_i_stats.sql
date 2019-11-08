-- Statistikk tabeller
alter table SYKEFRAVAR_STATISTIKK_LAND
    add column last_name numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_SEKTOR
    add column last_name numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_NARING
    add column last_name numeric(17,0) not null default 0;

alter table SYKEFRAVAR_STATISTIKK_VIRKSOMHET
    add column last_name numeric(17,0) not null default 0;

