package edu.stanford.protege.gateway.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.stanford.protege.webprotege.common.UserId;

import java.io.IOException;

public class UserIdSerializer extends StdSerializer<UserId> {

    public UserIdSerializer() {
        this(null);
    }

    public UserIdSerializer(Class<UserId> t) {
        super(t);
    }

    @Override
    public void serialize(UserId userId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(userId.id());
    }
}
