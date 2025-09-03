package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public class GameValue implements Value {
    private final String name;
    @Nullable
    private final String selector;

    public GameValue(String name, String selector) {
        this.name = name;
        this.selector = selector;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getSelector() {
        return selector;
    }

    public boolean hasSelector() {
        return selector != null;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("<");
        if (hasSelector()) {
            writer.write(selector);
        }
        writer.write(">");
        writer.write(name);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.GAME.getName());
        writer.name("game_value");
        writer.value(name);
        writer.name("selection");
        if (hasSelector()) {
            writer.value("{\"type\":\"" + selector + "\"}");
        } else {
            writer.value("null");
        }
        writer.endObject();
    }
}
