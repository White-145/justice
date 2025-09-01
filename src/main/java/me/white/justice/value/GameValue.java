package me.white.justice.value;

public class GameValue implements Value {
    private final String selector;
    private final String name;

    public GameValue(String selector, String name) {
        this.selector = selector;
        this.name = name;
    }

    public boolean hasSelector() {
        return selector != null;
    }

    public String getSelector() {
        return selector;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.GAME;
    }
}
