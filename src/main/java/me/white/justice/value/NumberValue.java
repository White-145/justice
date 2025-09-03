package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public class NumberValue implements Value {
    private final double number;
    @Nullable
    private final String placeholder;

    public NumberValue(double number) {
        placeholder = null;
        this.number = number;
    }

    public NumberValue(String placeholder) {
        this.placeholder = placeholder;
        number = 0;
    }

    public double getNumber() {
        return number;
    }

    @Nullable
    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isPlaceholder() {
        return placeholder != null;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (isPlaceholder()) {
            writer.write(placeholder);
        } else {
            writer.write(Double.toString(number));
        }
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.NUMBER.getName());
        writer.name("number");
        if (isPlaceholder()) {
            writer.value(placeholder);
        } else {
            writer.value(number);
        }
        writer.endObject();
    }
}
