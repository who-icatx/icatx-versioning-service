package edu.stanford.protege.versioning.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "BlacklistedIris")
public class BlacklistedIri {

    @Id
    private String id;
    
    @Field("iri")
    private String iri;

    public BlacklistedIri() {}

    public BlacklistedIri(String iri) {
        this.iri = iri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }
}
