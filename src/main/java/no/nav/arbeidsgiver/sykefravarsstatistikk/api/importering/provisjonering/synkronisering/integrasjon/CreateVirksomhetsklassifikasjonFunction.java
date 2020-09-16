package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.synkronisering.integrasjon;

@FunctionalInterface
public interface CreateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t);
}
