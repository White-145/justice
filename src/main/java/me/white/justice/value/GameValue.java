package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class GameValue implements Value {
    private final String selector;
    private final String name;

    public GameValue(String selector, String name) {
        this.selector = selector;
        this.name = name;
    }

    public boolean hasSelector() {
        return selector != null;
    }

    public String getSelector() {
        return selector;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.GAME;
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
        writer.value(getType().getName());
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
