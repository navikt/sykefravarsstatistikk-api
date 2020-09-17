package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

import java.util.Optional;

@FunctionalInterface
public interface FetchVirksomhetsklassifikasjonFunction<T> {
    Optional<T> apply(T t);
}
