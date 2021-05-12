create table kafka_utsending_historikk (
    orgnr      varchar  not null,
    key_json   varchar not null,
    value_json varchar not null,
    opprettet  timestamp default current_timestamp
);
