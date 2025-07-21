package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityChildren(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("entityUri") String entityUri,
        @JsonProperty("children") List<String> children
) {
    public static EntityChildren create(
            String projectId,
            String entityUri,
            List<String> children) {
        return new EntityChildren(projectId, entityUri, children);
    }
}
