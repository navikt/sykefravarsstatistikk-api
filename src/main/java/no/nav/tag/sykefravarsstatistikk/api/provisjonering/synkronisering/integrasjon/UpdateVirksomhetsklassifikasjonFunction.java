package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon;

@FunctionalInterface
public interface UpdateVirksomhetsklassifikasjonFunction<T> {
    int apply(T t1, T t2);
}
