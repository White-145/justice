package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class EnumValue implements Value {
    private final String name;

    public EnumValue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.ENUM;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.name("enum");
        writer.value(name);
    }

    @Override
    public String toString() {
        return "'" + name + "'";
    }
}
