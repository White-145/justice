package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class TextValue implements Value {
    private final String text;
    private final TextParsing textParsing;

    public TextValue(String text, TextParsing textParsing) {
        this.text = text;
        this.textParsing = textParsing;
    }

    public String getText() {
        return text;
    }

    public TextParsing getTextParsing() {
        return textParsing;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (textParsing != TextParsing.PLAIN) {
            writer.write(textParsing.getPrefix());
        }
        Value.writeEnclosed(writer, text, "\"");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.TEXT.getName());
        writer.name("text");
        writer.value(text);
        writer.name("parsing");
        writer.value(textParsing.getName());
        writer.endObject();
    }
}
