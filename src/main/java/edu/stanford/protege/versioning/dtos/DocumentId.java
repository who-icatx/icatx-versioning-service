package edu.stanford.protege.versioning.dtos;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/05/2012
 * <p>
 * Identifies a document that exists on a WebProtege webprotege.
 * </p>
 */
public record DocumentId(String documentId) implements Serializable {

    /**
     * Constructs an ServerFileId.
     *
     * @param documentId A string that identifies a document on the webprotege.  This string just acts as a "handle" to a file - it does
     *                   not reveal location specific information.  Not <code>null</code>.
     * @throws NullPointerException is documentId is <code>null</code>.
     */
    @JsonCreator
    public DocumentId {
        if (documentId == null) {
            throw new NullPointerException("documentId must not be null");
        }
    }

    /**
     * Gets the document id.
     *
     * @return A string that identifies the document.  This string just acts as a "handle" to a document - it does
     * not reveal location specific information (assuming the document actually exists as a file on the webprotege).
     */
    @Override
    @JsonValue
    public String documentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DocumentId other)) {
            return false;
        }
        return other.documentId.equals(this.documentId);
    }

    @Override
    public String toString() {
        return "DocumentId(" +
                documentId +
                ")";
    }
}
