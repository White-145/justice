package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class SoundValue implements Value {
    private final String name;
    private final double volume;
    private final double pitch;
    private final String source;
    private final String variant;

    public SoundValue(String name, double volume, double pitch, String source, String variant) {
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
        this.source = source;
        this.variant = variant;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        lexer.expect(TokenType.BLOCK_OPEN);
        String name = (String)lexer.expect(TokenType.STRING).getValue();
        double volume = 1;
        double pitch = 1;
        String source = null;
        String variant = null;
        Set<String> seenComponents = new HashSet<>();
        while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
            lexer.expect(TokenType.COMMA);
            String component = (String)lexer.expect(TokenType.LITERAL).getValue();
            if (seenComponents.contains(component)) {
                throw new CompilationException("Duplicate sound component '" + component + "'");
            }
            seenComponents.add(component);
            lexer.expect(TokenType.EQUALS);
            switch (component) {
                case "volume" -> volume = (double)lexer.expect(TokenType.NUMBER).getValue();
                case "pitch" -> pitch = (double)lexer.expect(TokenType.NUMBER).getValue();
                case "source" -> source = (String)lexer.expect(TokenType.STRING).getValue();
                case "variant" -> variant = (String)lexer.expect(TokenType.STRING).getValue();
                default -> throw new CompilationException("Invalid sound component '" + component + "'");
            }
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new SoundValue(name, volume, pitch, source, variant);
    }

    public String getName() {
        return name;
    }

    public double getVolume() {
        return volume;
    }

    public double getPitch() {
        return pitch;
    }

    public boolean hasSource() {
        return source != null;
    }

    public String getSource() {
        return source;
    }

    public boolean hasVariant() {
        return variant != null;
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public ValueType getType() {
        return ValueType.SOUND;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("sound{ ");
        Value.writeString(writer, name);
        if (volume != 1) {
            writer.write(", volume=");
            Value.writeNumber(writer, volume);
        }
        if (pitch != 1) {
            writer.write(", pitch=");
            Value.writeNumber(writer, pitch);
        }
        if (hasSource()) {
            writer.write(", source=\"");
            writer.write(source);
            writer.write("\"");
        }
        if (hasVariant()) {
            writer.write(", variant=\"");
            writer.write(variant);
            writer.write("\"");
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("sound");
        writer.value(name);
        writer.name("pitch");
        writer.value(pitch);
        writer.name("volume");
        writer.value(volume);
        if (hasVariant()) {
            writer.name("variation");
            writer.value(variant);
        }
        if (hasSource()) {
            writer.name("source");
            writer.value(source);
        }
        writer.endObject();
    }
}
