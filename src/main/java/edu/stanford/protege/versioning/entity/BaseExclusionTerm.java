package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaseExclusionTerm(@JsonProperty("label") String label,
                                @JsonProperty("foundationReference") String foundationReference,
                                @JsonProperty("termId") String termId) {
}
