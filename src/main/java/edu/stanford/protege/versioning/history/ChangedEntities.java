package edu.stanford.protege.versioning.history;

import java.util.List;

public record ChangedEntities(List<String> createdEntities,
                              List<String> updatedEntities,
                              List<String> deletedEntities) {
}
