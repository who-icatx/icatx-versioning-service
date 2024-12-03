package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LanguageTerm(@JsonProperty("label") String label, @JsonProperty("termId") String termId) {
}
