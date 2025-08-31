package me.white.justice.parser;

import com.google.gson.stream.JsonWriter;
import me.white.justice.value.VariableValue;

import java.io.IOException;
import java.util.List;

public class Handler {
    private final HandlerType type;
    private final String name;
    private final List<Operation> operations;

    public Handler(HandlerType type, String name, List<Operation> operations) {
        this.type = type;
        this.name = name;
        this.operations = operations;
    }

    public HandlerType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Operation> getInstructions() {
        return operations;
    }

    public void write(JsonWriter writer) throws IOException {
        writer.name("type");
        writer.value(type.name);
        if (type == HandlerType.EVENT) {
            writer.name("event");
        } else {
            writer.name("name");
        }
        writer.value(name);
        writer.name("operations");
        writer.beginArray();
        for (Operation operation : operations) {
            writer.beginObject();
            operation.write(writer);
            writer.endObject();
        }
        writer.endArray();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (type != HandlerType.FUNCTION) {
            builder.append(type.name);
            builder.append(" ");
        }
        builder.append(VariableValue.identifierToString(name, false));
        builder.append(" {\n");
        for (Operation instr : operations) {
            String[] lines = instr.toString().split("\n");
            for (String line : lines) {
                builder.append("    ");
                builder.append(line);
                builder.append("\n");
            }
        }
        builder.append("}");
        return builder.toString();
    }
}
