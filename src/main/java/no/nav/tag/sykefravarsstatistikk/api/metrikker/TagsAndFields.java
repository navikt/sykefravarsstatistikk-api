package no.nav.tag.sykefravarsstatistikk.api.metrikker;

import lombok.Value;

import java.util.Map;

@Value
public class TagsAndFields {
    private final Map<String, Object> fields;
    private final Map<String, String> tags;
}
