package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class ArrayValue extends ArrayList<Value> implements Value {
    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("{ ");
        for (int i = 0; i < size(); ++i) {
            get(i).write(writer);
            if (i != size() - 1) {
                writer.write(", ");
            }
        }
        if (!isEmpty()) {
            writer.write(" ");
        }
        writer.write("}");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("values");
        writer.beginArray();
        for (Value innerValue : this) {
            if (innerValue.getType() == ValueType.ARRAY) {
                throw null; // no nested arrays allowed;
            }
            innerValue.writeJson(writer);
        }
        writer.endArray();
        writer.endObject();
    }
}
