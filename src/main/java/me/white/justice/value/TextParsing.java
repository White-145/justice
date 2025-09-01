package me.white.justice.value;

public enum TextParsing {
    PLAIN('p', "plain"),
    LEGACY('l', "legacy"),
    MINIMESSAGE('m', "minimessage"),
    JSON('j', "json");

    final char prefix;
    final String name;

    TextParsing(char prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public static TextParsing byPrefix(char prefix) {
        for (TextParsing textParsing : TextParsing.values()) {
            if (textParsing.prefix == prefix) {
                return textParsing;
            }
        }
        return null;
    }

    public static TextParsing byName(String name) {
        for (TextParsing textParsing : TextParsing.values()) {
            if (textParsing.name.equals(name)) {
                return textParsing;
            }
        }
        return null;
    }

    public char getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }
}
