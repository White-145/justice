package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

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
    public void write(Writer writer) throws IOException {
        writer.write("'");
        writer.write(name.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\t", "\\t"));
        writer.write("'");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("enum");
        writer.value(name);
        writer.endObject();
    }
}
