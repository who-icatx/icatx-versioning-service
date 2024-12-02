package edu.stanford.protege.versioning.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public class IriDeserializer extends StdDeserializer<IRI> {

    public IriDeserializer() {
        super(IRI.class);
    }

    @Override
    public IRI deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getValueAsString();
        if(value == null) {
            return null;
        }
        return IRI.create(jsonParser.getValueAsString());
    }
}

