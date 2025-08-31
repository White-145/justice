package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

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

    @Override
    public ValueType getType() {
        return ValueType.NUMBER;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.name("number");
        if (placeholder != null) {
            writer.value(placeholder);
        } else {
            writer.value(number);
        }
    }

    @Override
    public String toString() {
        if (placeholder != null) {
            return placeholder;
        }
        return Double.toString(number);
    }
}
