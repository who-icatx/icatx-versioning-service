package edu.stanford.protege.versioning.entity;

import java.util.List;

public record EntityLogicalDefinition(String logicalDefinitionSuperclass, List<LogicalConditionRelationship> relationships ) {
}
