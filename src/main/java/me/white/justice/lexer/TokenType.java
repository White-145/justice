package me.white.justice.lexer;

public enum TokenType {
    LITERAL("literal"), // keyword
    IDENTIFIER("identifier"), // `identifier`

    STRING("string"), // "string"
    NUMBER("number"), // 123.456
    PLACEHOLDER("placeholder"), // %placeholder%
    COLOR("color"), // #6495ED
    ENUM("enum"), // 'enum'

    BLOCK_OPEN("'{'"), // '{'
    BLOCK_CLOSE("'}'"), // '}'
    ARGS_OPEN("'('"), // '('
    ARGS_CLOSE("')'"), // ')'
    SELECTOR_OPEN("'<'"), // '<'
    SELECTOR_CLOSE("'>'"), // '>'
    COMMA("','"), // ','
    EQUALS("'='"), // '='
    EOL("';'"), // ';'

    EOF("<eof>");

    final String representation;

    TokenType(String representation) {
        this.representation = representation;
    }

    public boolean isIdentifier() {
        return this == LITERAL || this == IDENTIFIER;
    }

    public boolean isString() {
        return this == LITERAL || this == IDENTIFIER || this == STRING || this == PLACEHOLDER || this == ENUM;
    }

    public boolean isNumber() {
        return this == NUMBER || this == COLOR;
    }

    public boolean isInteger() {
        return this == COLOR;
    }
}
