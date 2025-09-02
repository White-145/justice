package me.white.justice;

import com.google.gson.*;
import me.white.justice.parser.Handler;
import me.white.justice.parser.HandlerType;
import me.white.justice.parser.Operation;
import me.white.justice.value.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Decompiler {
    private final List<Handler> handlers = new ArrayList<>();

    public Decompiler() { }

    public void decompile(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        JsonArray handlersArray = object.get("handlers").getAsJsonArray();
        for (JsonElement element : handlersArray) {
            decompileHandler(element.getAsJsonObject());
        }
    }

    private void decompileHandler(JsonObject handler) {
        HandlerType type = HandlerType.byName(handler.get("type").getAsString());
        if (type == null) {
            throw null; // invalid handler type
        }
        String name;
        if (type == HandlerType.EVENT) {
            name = handler.get("event").getAsString();
        } else {
            name = handler.get("name").getAsString();
        }
        List<Operation> operations = new ArrayList<>();
        JsonArray operationsArray = handler.get("operations").getAsJsonArray();
        for (JsonElement element : operationsArray) {
            JsonObject operation = element.getAsJsonObject();
            operations.add(decompileOperation(operation));
        }
        handlers.add(new Handler(type, name, operations));
    }

    private static Operation decompileOperation(JsonObject operation) {
        String name = operation.get("action").getAsString();
        String delegate = null;
        boolean isInverted = false;
        String selector = null;
        Map<String, Value> arguments = new HashMap<>();
        List<Operation> operations = new ArrayList<>();
        if (operation.has("conditional")) {
            JsonObject conditional = operation.get("conditional").getAsJsonObject();
            delegate = conditional.get("action").getAsString();
            if (conditional.has("is_inverted")) {
                isInverted = conditional.get("is_inverted").getAsBoolean();
            }
        } else if (operation.has("is_inverted")) {
            isInverted = operation.get("is_inverted").getAsBoolean();
        }
        if (operation.has("selection")) {
            JsonObject selection = operation.get("selection").getAsJsonObject();
            selector = selection.get("type").getAsString();
        }
        JsonArray values = operation.get("values").getAsJsonArray();
        for (JsonElement element : values) {
            JsonObject argument = element.getAsJsonObject();
            String valueName = argument.get("name").getAsString();
            JsonObject valueObject = argument.get("value").getAsJsonObject();
            Value value = decompileValue(valueObject, true);
            if (value != null) {
                arguments.put(valueName, value);
            }
        }
        if (operation.has("operations")) {
            JsonArray operationsArray = operation.get("operations").getAsJsonArray();
            for (JsonElement element : operationsArray) {
                JsonObject operationObject = element.getAsJsonObject();
                operations.add(decompileOperation(operationObject));
            }
        }
        return new Operation(isInverted, name, selector, arguments, operations, delegate);
    }

    private static Value decompileValue(JsonObject value, boolean allowArrays) {
        if (value.isEmpty()) {
            return null;
        }
        ValueType type = ValueType.byName(value.get("type").getAsString());
        if (type == null) {
            throw null; // invalid type
        }
        if (type == ValueType.ARRAY) {
            if (!allowArrays) {
                throw null; // no recursing arrays
            }
            ArrayValue array = new ArrayValue();
            JsonArray values = value.get("values").getAsJsonArray();
            for (JsonElement element : values) {
                JsonObject object = element.getAsJsonObject();
                Value innerValue = decompileValue(object, false);
                if (innerValue != null) {
                    array.add(innerValue);
                }
            }
            return array;
        }
        return type.readJson(value);
    }

    public void write(Writer writer) throws IOException {
        for (Handler handler : handlers) {
            writeHandler(writer, handler);
            writer.write("\n");
        }
    }

    private static void writeHandler(Writer writer, Handler handler) throws IOException {
        if (handler.getType() != HandlerType.FUNCTION) {
            writer.write(handler.getType().getName());
            writer.write(" ");
        }
        Value.writeIdentifier(writer, handler.getName(), false);
        writer.write(" {\n");
        for (Operation operation : handler.getOperations()) {
            writeOperation(writer, operation, 1);
        }
        writer.write("}\n");
    }

    private static void writeOperation(Writer writer, Operation operation, int level) throws IOException {
        for (int i = 0; i < level; ++i) {
            writer.write("    ");
        }
        if (operation.hasDelegate()) {
            writer.write(operation.getName());
            if (operation.isInverted()) {
                writer.write(" not");
            }
            writer.write(" ");
            writer.write(operation.getDelegate());
        } else {
            if (operation.isInverted()) {
                writer.write("not ");
            }
            writer.write(operation.getName());
        }
        if (operation.hasSelector()) {
            writer.write("<");
            writer.write(operation.getSelector());
            writer.write(">");
        }
        Map<String, Value> arguments = operation.getArguments();
        if (!arguments.isEmpty()) {
            writer.write("(");
            int i = 0;
            for (Map.Entry<String, Value> argument : arguments.entrySet()) {
                writer.write(argument.getKey());
                writer.write("=");
                argument.getValue().write(writer);
                if (i != arguments.size() - 1) {
                    writer.write(", ");
                }
                i += 1;
            }
            writer.write(")");
        }
        List<Operation> operations = operation.getOperations();
        if (!operations.isEmpty()) {
            writer.write(" {");
            writer.write("\n");
            for (Operation innerOperation : operations) {
                writeOperation(writer, innerOperation, level + 1);
            }
            for (int i = 0; i < level; ++i) {
                writer.write("    ");
            }
            writer.write("}");
        } else {
            writer.write(";");
        }
        writer.write("\n");
    }
}
