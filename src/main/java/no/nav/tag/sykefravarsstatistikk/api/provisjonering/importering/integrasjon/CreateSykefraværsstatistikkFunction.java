package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon;

@FunctionalInterface
public interface CreateSykefraværsstatistikkFunction<T> {
    int apply(T t);
}
