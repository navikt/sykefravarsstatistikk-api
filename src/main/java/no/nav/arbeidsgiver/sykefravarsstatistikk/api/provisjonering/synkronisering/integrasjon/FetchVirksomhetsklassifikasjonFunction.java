package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon;

import java.util.Optional;

@FunctionalInterface
public interface FetchVirksomhetsklassifikasjonFunction<T> {
    Optional<T> apply(T t);
}
