package me.white.justice.lexer;

import org.jetbrains.annotations.Nullable;

public class Token {
    private final TokenType type;
    @Nullable
    private final Object value;
    private final int pos;
    private final int row;
    private final int column;
    private final int length;

    public Token(TokenType type, Object value, int pos, int row, int column, int length) {
        this.type = type;
        this.value = value;
        this.pos = pos;
        this.row = row;
        this.column = column;
        this.length = length;
    }

    public TokenType getType() {
        return type;
    }

    @Nullable
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

    public int getLength() {
        return length;
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
