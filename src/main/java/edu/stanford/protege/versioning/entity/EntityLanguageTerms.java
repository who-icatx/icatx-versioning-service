package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityLanguageTerms(@JsonProperty("title") LanguageTerm title,
                                  @JsonProperty("definition") LanguageTerm definition,
                                  @JsonProperty("longDefinition") LanguageTerm longDefinition,
                                  @JsonProperty("fullySpecifiedName") LanguageTerm fullySpecifiedName,
                                  @JsonProperty("baseIndexTerms") List<BaseIndexTerm> baseIndexTerms,
                                  @JsonProperty("subclassBaseInclusions") List<String> subclassBaseInclusions,

                                  @JsonProperty("baseExclusionTerms") List<BaseExclusionTerm> baseExclusionTerms,
                                  @JsonProperty("isObsolete") boolean isObsolete

) {
}
