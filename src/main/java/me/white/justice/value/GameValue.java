package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class GameValue implements Value {
    private final String selector;
    private final String name;

    public GameValue(String selector, String name) {
        this.selector = selector;
        this.name = name;
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
    public void write(JsonWriter writer) throws IOException {
        writer.name("game_value");
        writer.value(name);
        writer.name("selection");
        writer.value(selector == null ? "null" : "{\"type\":\"" + selector + "\"}");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        if (selector != null) {
            builder.append(selector);
        }
        builder.append(">");
        builder.append(name);
        return builder.toString();
    }
}
