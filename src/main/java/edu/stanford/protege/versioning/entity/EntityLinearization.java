package edu.stanford.protege.versioning.entity;

import jakarta.validation.constraints.NotNull;

public record EntityLinearization(String isAuxiliaryAxisChild,
                                  String isGrouping,
                                  String isIncludedInLinearization,
                                  String linearizationPathParent,
                                  @NotNull String linearizationId,
                                  String codingNote) {

}
