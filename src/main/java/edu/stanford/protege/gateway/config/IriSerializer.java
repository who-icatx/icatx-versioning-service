package edu.stanford.protege.gateway.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;

public class IriSerializer extends StdSerializer<IRI> {

    public IriSerializer() {
        this(null);
    }

    public IriSerializer(Class<IRI> t) {
        super(t);
    }

    @Override
    public void serialize(IRI iri, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(iri.toString());
    }
}
