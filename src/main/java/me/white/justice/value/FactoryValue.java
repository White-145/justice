package me.white.justice.value;

import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;

public enum FactoryValue {
    LOCATION("location"),
    VECTOR("vector"),
    ITEM("item"),
    SOUND("sound"),
    POTION("potion"),
    PARTICLE("particle");

    final String name;

    FactoryValue(String name) {
        this.name = name;
    }

    public static FactoryValue byName(String name) {
        for (FactoryValue value : FactoryValue.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public Value parse(Lexer lexer) throws CompilationException {
        return switch (this) {
            case LOCATION -> LocationValue.parse(lexer);
            case VECTOR -> VectorValue.parse(lexer);
            case ITEM -> ItemValue.parse(lexer);
            case SOUND -> SoundValue.parse(lexer);
            case POTION -> PotionValue.parse(lexer);
            case PARTICLE -> ParticleValue.parse(lexer);
        };
    }
}
