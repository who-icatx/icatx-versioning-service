package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LogicalConditionsFunctionalOwl(@JsonProperty("owlSyntax") String owlSyntax,
                                             @JsonProperty("axioms") List<String> axioms) {
}
