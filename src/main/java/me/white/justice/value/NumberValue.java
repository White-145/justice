package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class NumberValue implements Value {
    private final String placeholder;
    private final double number;

    public NumberValue(String placeholder) {
        this.placeholder = placeholder;
        number = 0;
    }

    public NumberValue(double number) {
        placeholder = null;
        this.number = number;
    }

    public boolean isPlaceholder() {
        return placeholder != null;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public double getNumber() {
        return number;
    }

    @Override
    public ValueType getType() {
        return ValueType.NUMBER;
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
        writer.value(getType().getName());
        writer.name("number");
        if (isPlaceholder()) {
            writer.value(placeholder);
        } else {
            writer.value(number);
        }
        writer.endObject();
    }
}
