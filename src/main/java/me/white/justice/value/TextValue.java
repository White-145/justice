package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class TextValue implements Value {
    private final TextParsing textParsing;
    private final String text;

    public TextValue(TextParsing textParsing, String text) {
        this.textParsing = textParsing;
        this.text = text;
    }

    public TextParsing getTextParsing() {
        return textParsing;
    }

    public String getText() {
        return text;
    }

    @Override
    public ValueType getType() {
        return ValueType.TEXT;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (textParsing != TextParsing.PLAIN) {
            writer.write(textParsing.getPrefix());
        }
        Value.writeString(writer, text);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("text");
        writer.value(text);
        writer.name("parsing");
        writer.value(textParsing.getName());
        writer.endObject();
    }
}
