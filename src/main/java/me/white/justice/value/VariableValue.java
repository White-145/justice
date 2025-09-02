package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class VariableValue implements Value {
    private final VariableScope scope;
    private final String name;

    public VariableValue(VariableScope scope, String name) {
        this.scope = scope;
        this.name = name;
    }

    public VariableScope getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    @Override
    public void write(Writer writer) throws IOException {
        boolean nonLocal = scope != VariableScope.LOCAL;
        if (nonLocal) {
            writer.write(scope.getPrefix());
        }
        Value.writeIdentifier(writer, name, nonLocal);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.VARIABLE.getName());
        writer.name("variable");
        writer.value(name);
        writer.name("scope");
        writer.value(scope.getName());
        writer.endObject();
    }
}
