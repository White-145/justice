package me.white.justice;

import com.google.gson.stream.JsonWriter;
import me.white.justice.lexer.Lexer;
import me.white.justice.parser.Handler;
import me.white.justice.parser.HandlerType;
import me.white.justice.parser.Operation;
import me.white.justice.parser.Parser;
import me.white.justice.value.Value;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Compiler {
    private final List<Handler> handlers = new ArrayList<>();
    private boolean isCompact = false;

    public Compiler() { }

    public boolean isCompact() {
        return isCompact;
    }

    public void setCompact(boolean compact) {
        isCompact = compact;
    }

    public void compile(String source) throws CompilationException {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer);
        handlers.addAll(parser.parse());
    }

    public void write(Writer writer) throws IOException {
        try (JsonWriter json = new JsonWriter(writer)) {
            json.beginObject();
            json.name("handlers");
            json.beginArray();
            for (int i = 0; i < handlers.size(); ++i) {
                writeHandler(json, handlers.get(i), isCompact ? 0 : i);
            }
            json.endArray();
            json.endObject();
        }
    }

    private static void writeHandler(JsonWriter writer, Handler handler, int position) throws IOException {
        writer.beginObject();
        writer.name("position");
        writer.value(position);
        writer.name("type");
        HandlerType type = handler.getType();
        writer.value(type.getName());
        if (type == HandlerType.EVENT) {
            writer.name("event");
        } else {
            writer.name("name");
        }
        writer.value(handler.getName());
        writer.name("operations");
        writer.beginArray();
        for (Operation operation : handler.getOperations()) {
            writeOperation(writer, operation);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeOperation(JsonWriter writer, Operation operation) throws IOException {
        writer.beginObject();
        writer.name("action");
        writer.value(operation.getName());
        if (operation.hasDelegate()) {
            writer.name("conditional");
            writer.beginObject();
            writer.name("action");
            writer.value(operation.getDelegate());
            writer.name("is_inverted");
            writer.value(operation.isInverted());
            writer.endObject();
        } else if (operation.isInverted()) {
            writer.name("is_inverted");
            writer.value(true);
        }
        if (operation.hasSelector()) {
            writer.name("selection");
            writer.beginObject();
            writer.name("type");
            writer.value(operation.getSelector());
            writer.endObject();
        }
        writer.name("values");
        writer.beginArray();
        if (!operation.getArguments().isEmpty()) {
            for (Map.Entry<String, Value> entry : operation.getArguments().entrySet()) {
                writer.beginObject();
                writer.name("name");
                writer.value(entry.getKey());
                writer.name("value");
                entry.getValue().writeJson(writer);
                writer.endObject();
            }
        }
        writer.endArray();
        if (!operation.getOperations().isEmpty()) {
            writer.name("operations");
            writer.beginArray();
            for (Operation innerOperation : operation.getOperations()) {
                writeOperation(writer, innerOperation);
            }
            writer.endArray();
        }
        writer.endObject();
    }
}
