package me.white.justice.value;

import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

import java.util.HashSet;
import java.util.Set;

public class PotionValue implements Value {
    private final String name;
    private final int amplifier;
    private final int duration;

    public PotionValue(String name, int amplifier, int duration) {
        this.name = name;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        lexer.expect(TokenType.BLOCK_OPEN);
        String name = (String)lexer.expect(TokenType.STRING).getValue();
        int amplifier = 0;
        int duration = -1;
        Set<String> seenComponents = new HashSet<>();
        while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
            lexer.expect(TokenType.COMMA);
            String component = (String)lexer.expect(TokenType.LITERAL).getValue();
            if (seenComponents.contains(component)) {
                throw new CompilationException("Duplicate potion component '" + component + "'");
            }
            seenComponents.add(component);
            lexer.expect(TokenType.EQUALS);
            switch (component) {
                case "amplifier" -> amplifier = (int)lexer.expect(TokenType.NUMBER).getValue();
                case "duration" -> duration = (int)lexer.expect(TokenType.NUMBER).getValue();
                default -> throw new CompilationException("Invalid potion component '" + component + "'");
            }
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new PotionValue(name, amplifier, duration);
    }

    public String getName() {
        return name;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public ValueType getType() {
        return ValueType.POTION;
    }
}
