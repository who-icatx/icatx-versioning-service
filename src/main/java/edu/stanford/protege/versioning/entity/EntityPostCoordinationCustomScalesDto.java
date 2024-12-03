package edu.stanford.protege.versioning.entity;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EntityPostCoordinationCustomScalesDto(List<String> postcoordinationScaleValues,
                                                    @NotNull String postcoordinationAxis) {
}
