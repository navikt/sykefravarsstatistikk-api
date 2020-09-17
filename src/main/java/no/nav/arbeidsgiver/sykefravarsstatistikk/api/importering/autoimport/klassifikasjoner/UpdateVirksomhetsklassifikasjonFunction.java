package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

@FunctionalInterface
public interface UpdateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t1, T t2);
}
