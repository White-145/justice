package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TextValue implements Value {
    private final TextParsing textParsing;
    private final String text;

    public TextValue(TextParsing textParsing, String text) {
        this.textParsing = textParsing;
        this.text = text;
    }

    public TextParsing getParsing() {
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
    public void write(JsonWriter writer) throws IOException {
        writer.name("text");
        writer.value(text);
        writer.name("parsing");
        writer.value(textParsing.name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (textParsing != TextParsing.PLAIN) {
            builder.append(textParsing.prefix);
        }
        builder.append("\"");
        builder.append(text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t"));
        builder.append("\"");
        return builder.toString();
    }
}
