package edu.stanford.protege.versioning.entity;

import java.util.List;

public record LogicalConditions(List<EntityLogicalDefinition> logicalDefinitions,
                                List<LogicalConditionRelationship> necessaryConditions) {
}
