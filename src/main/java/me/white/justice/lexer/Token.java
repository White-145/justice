package me.white.justice.lexer;

public class Token {
    private final TokenType type;
    private final Object value;
    private final int pos;
    private final int row;
    private final int column;

    public Token(TokenType type, Object value, int pos, int row, int column) {
        this.type = type;
        this.value = value;
        this.pos = pos;
        this.row = row;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getString() {
        if (!type.isString()) {
            return null;
        }
        return (String)value;
    }

    public double getNumber() {
        if (!type.isNumber()) {
            return 0;
        }
        return (double)value;
    }

    public int getInteger() {
        if (!type.isInteger()) {
            return 0;
        }
        return (int)value;
    }

    public int getPos() {
        return pos;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isOf(TokenType type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "Token{ " + type + " }";
        }
        return "Token{ " + type + ": " + value + " }";
    }
}
