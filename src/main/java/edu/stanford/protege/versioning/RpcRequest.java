package edu.stanford.protege.versioning;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-21
 */
public record RpcRequest(RpcMethod method, ObjectNode params) {

    public RpcRequest {
        if(params == null) {
            params = new ObjectNode(new JsonNodeFactory(true));
        }
    }

    @JsonIgnore
    String methodName() {
        return this.method.getMethodName();
    }
}
