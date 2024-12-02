package edu.stanford.protege.versioning.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.stanford.protege.webprotege.common.UserId;

import java.io.IOException;

public class UserIdDeserializer extends StdDeserializer<UserId> {

    public UserIdDeserializer() {
        super(UserId.class);
    }

    @Override
    public UserId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getValueAsString();
        if (value == null) {
            return null;
        }
        return UserId.valueOf(jsonParser.getValueAsString());
    }
}
