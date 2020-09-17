package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

@FunctionalInterface
public interface CreateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t);
}
