package me.white.justice;

import com.google.gson.*;
import me.white.justice.value.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Marshal {
    private final List<Handler> handlers = new ArrayList<>();

    public Marshal() { }

    public static JsonElement jsonGet(JsonObject object, String name) throws MarshalException {
        JsonElement element = object.get(name);
        if (element == null) {
            throw new MarshalException("No '" + name + "' member");
        }
        return element;
    }

    public void marshal(String json) throws MarshalException {
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            JsonArray handlersArray = jsonGet(object, "handlers").getAsJsonArray();
            for (JsonElement element : handlersArray) {
                handlers.add(marshalHandler(element.getAsJsonObject()));
            }
        } catch (JsonParseException | IllegalStateException | MarshalException e) {
            throw new MarshalException("Malformed module: " + e.getMessage());
        }
    }

    public static Handler marshalHandler(JsonObject handler) throws MarshalException {
        String typeString = jsonGet(handler, "type").getAsString();
        HandlerType type = HandlerType.byName(typeString);
        if (type == null) {
            throw new MarshalException("Invalid handler type '" + typeString + "'");
        }
        String name;
        if (type == HandlerType.EVENT) {
            name = jsonGet(handler, "event").getAsString();
        } else {
            name = jsonGet(handler, "name").getAsString();
        }
        List<Operation> operations = new ArrayList<>();
        JsonArray operationsArray = jsonGet(handler, "operations").getAsJsonArray();
        for (JsonElement element : operationsArray) {
            JsonObject operation = element.getAsJsonObject();
            operations.add(marshalOperation(operation));
        }
        return new Handler(type, name, operations);
    }

    public static Operation marshalOperation(JsonObject operation) throws MarshalException {
        String name = jsonGet(operation, "action").getAsString();
        String delegate = null;
        boolean isInverted = false;
        String selector = null;
        Map<String, Value> arguments = new HashMap<>();
        List<Operation> operations = new ArrayList<>();
        if (operation.has("conditional")) {
            JsonObject conditional = jsonGet(operation, "conditional").getAsJsonObject();
            delegate = jsonGet(conditional, "action").getAsString();
            if (conditional.has("is_inverted")) {
                isInverted = jsonGet(conditional, "is_inverted").getAsBoolean();
            }
        } else if (operation.has("is_inverted")) {
            isInverted = jsonGet(operation, "is_inverted").getAsBoolean();
        }
        if (operation.has("selection")) {
            JsonObject selection = jsonGet(operation, "selection").getAsJsonObject();
            selector = jsonGet(selection, "type").getAsString();
        }
        JsonArray values = jsonGet(operation, "values").getAsJsonArray();
        for (JsonElement element : values) {
            JsonObject argument = element.getAsJsonObject();
            String valueName = jsonGet(argument, "name").getAsString();
            JsonObject valueObject = jsonGet(argument, "value").getAsJsonObject();
            Value value = marshalValue(valueObject, true);
            if (value != null) {
                arguments.put(valueName, value);
            }
        }
        if (operation.has("operations")) {
            JsonArray operationsArray = jsonGet(operation, "operations").getAsJsonArray();
            for (JsonElement element : operationsArray) {
                JsonObject operationObject = element.getAsJsonObject();
                operations.add(marshalOperation(operationObject));
            }
        }
        return new Operation(isInverted, name, selector, arguments, operations, delegate);
    }

    public static Value marshalValue(JsonObject value, boolean allowArrays) throws MarshalException {
        if (value.isEmpty()) {
            return null;
        }
        String typeString = jsonGet(value, "type").getAsString();
        ValueType type = ValueType.byName(typeString);
        if (type == null) {
            throw new MarshalException("Invalid value type '" + typeString + "'");
        }
        if (type == ValueType.ARRAY && !allowArrays) {
            throw new MarshalException("Recursing array values");
        }
        return type.marshal(value);
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
