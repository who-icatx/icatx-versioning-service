package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaseIndexTerm(@JsonProperty("label") String label,
                            @JsonProperty("indexType") String indexType,
                            @JsonProperty("isInclusion") boolean isInclusion,
                            @JsonProperty("termId") String termId
                            ) {
}
