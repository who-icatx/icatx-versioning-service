package edu.stanford.protege.versioning.repository;

import edu.stanford.protege.versioning.entity.ReproducibleProject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReproducibleProjectsRepository extends MongoRepository<ReproducibleProject, String> {

    public ReproducibleProject findByProjectId(String projectId);
}
