package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;

public class ArrayValue extends ArrayList<Value> implements Value {
    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.name("values");
        writer.beginArray();
        for (Value value : this) {
            writer.beginObject();
            writer.name("type");
            writer.value(value.getType().name);
            value.write(writer);
            writer.endObject();
        }
        writer.endArray();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{ ");
        for (int i = 0; i < size(); ++i) {
            builder.append(get(i));
            if (i != size() - 1) {
                builder.append(", ");
            }
        }
        if (!isEmpty()) {
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }
}
