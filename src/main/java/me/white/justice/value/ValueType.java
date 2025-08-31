package me.white.justice.value;

public enum ValueType {
    NUMBER("number"),
    TEXT("text"),
    ARRAY("array"),
    VARIABLE("variable"),
    GAME("game_value"),
    LOCATION("location"),
    VECTOR("vector"),
    ITEM("item"),
    SOUND("sound"),
    POTION("potion"),
    PARTICLE("particle"),
    ENUM("enum");

    public final String name;

    ValueType(String name) {
        this.name = name;
    }

    public static ValueType byName(String name) {
        for (ValueType type : ValueType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
