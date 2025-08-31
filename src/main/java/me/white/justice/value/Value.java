package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface Value {
    ValueType getType();

    void write(JsonWriter writer) throws IOException;
}
