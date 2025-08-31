package me.white.justice.parser;

import com.google.gson.stream.JsonWriter;
import me.white.justice.value.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Operation {
    private final boolean isInverted;
    private final String name;
    private final String delegate;
    private final String selector;
    private final Map<String, Value> arguments;
    private final List<Operation> operations;

    public Operation(boolean isInverted, String name, String selector, Map<String, Value> arguments, List<Operation> operations, String delegate) {
        this.isInverted = isInverted;
        this.name = name;
        this.selector = selector;
        this.arguments = arguments;
        this.operations = operations;
        this.delegate = delegate;
    }

    public String getName() {
        return name;
    }

    public String getSelector() {
        return selector;
    }

    public Map<String, Value> getArguments() {
        return arguments;
    }

    public List<Operation> getInstructions() {
        return operations;
    }

    public String getDelegate() {
        return delegate;
    }

    public void write(JsonWriter writer) throws IOException {
        writer.name("action");
        writer.value(name);
        if (delegate != null) {
            writer.name("conditional");
            writer.beginObject();
            writer.name("action");
            writer.value(delegate);
            writer.name("is_inverted");
            writer.value(isInverted);
            writer.endObject();
        } else if (isInverted) {
            writer.name("is_inverted");
            writer.value(true);
        }
        if (selector != null) {
            writer.name("selection");
            writer.beginObject();
            writer.name("type");
            writer.value(selector);
            writer.endObject();
        }
        writer.name("values");
        writer.beginArray();
        if (arguments != null) {
            for (Map.Entry<String, Value> entry : arguments.entrySet()) {
                writer.beginObject();
                writer.name("name");
                writer.value(entry.getKey());
                writer.name("value");
                writer.beginObject();
                writer.name("type");
                writer.value(entry.getValue().getType().name);
                entry.getValue().write(writer);
                writer.endObject();
                writer.endObject();
            }
        }
        writer.endArray();
        if (operations != null) {
            writer.name("operations");
            writer.beginArray();
            for (Operation operation : operations) {
                writer.beginObject();
                operation.write(writer);
                writer.endObject();
            }
            writer.endArray();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (delegate != null) {
            builder.append(name);
            if (isInverted) {
                builder.append(" not");
            }
            builder.append(" ");
            builder.append(delegate);
        } else {
            if (isInverted) {
                builder.append("not ");
            }
            builder.append(name);
        }
        if (selector != null) {
            builder.append("<");
            builder.append(selector);
            builder.append(">");
        }
        if (!arguments.isEmpty()) {
            builder.append("(");
            int i = 0;
            for (Map.Entry<String, Value> argument : arguments.entrySet()) {
                builder.append(argument.getKey());
                builder.append("=");
                builder.append(argument.getValue());
                if (i != arguments.size() - 1) {
                    builder.append(", ");
                }
                i += 1;
            }
            builder.append(")");
        }
        if (operations != null) {
            builder.append(" {");
            if (operations.isEmpty()) {
                builder.append(" ");
            } else {
                builder.append("\n");
                for (Operation operation : operations) {
                    String[] lines = operation.toString().split("\n");
                    for (String line : lines) {
                        builder.append("    ");
                        builder.append(line);
                        builder.append("\n");
                    }
                }
            }
            builder.append("}");
        } else {
            builder.append(";");
        }
        return builder.toString();
    }
}
