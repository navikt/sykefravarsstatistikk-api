package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.synkronisering.integrasjon;

@FunctionalInterface
public interface UpdateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t1, T t2);
}
