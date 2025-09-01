package me.white.justice.value;

import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

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
}
