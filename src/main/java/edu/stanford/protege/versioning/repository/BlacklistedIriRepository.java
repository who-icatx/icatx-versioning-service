package edu.stanford.protege.versioning.repository;

import edu.stanford.protege.versioning.entity.BlacklistedIri;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedIriRepository extends MongoRepository<BlacklistedIri, String> {
}
