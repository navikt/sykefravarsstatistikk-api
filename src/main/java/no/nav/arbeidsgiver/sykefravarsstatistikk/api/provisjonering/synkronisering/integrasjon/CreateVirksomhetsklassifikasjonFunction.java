package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon;

@FunctionalInterface
public interface CreateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t);
}
