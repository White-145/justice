package me.white.justice.value;

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
}
