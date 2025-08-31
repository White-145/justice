package me.white.justice.lexer;

public enum TokenType {
    LITERAL(true, "literal"), // keyword
    IDENTIFIER(true, "identifier"), // `identifier`

    STRING(false, "string"), // "string"
    NUMBER(false, "number"), // 123.456
    PLACEHOLDER(false, "placeholder"), // %placeholder%
    COLOR(false, "color"), // #6495ED
    ENUM(false, "enum"), // 'enum'

    BLOCK_OPEN(false, "'{'"), // '{'
    BLOCK_CLOSE(false, "'}'"), // '}'
    ARGS_OPEN(false, "'('"), // '('
    ARGS_CLOSE(false, "')'"), // ')'
    SELECTOR_OPEN(false, "'<'"), // '<'
    SELECTOR_CLOSE(false, "'>'"), // '>'
    COMMA(false, "','"), // ','
    EQUALS(false, "'='"), // '='
    EOL(false, "';'"), // ';'

    EOF(false, "");

    final boolean isIdentifier;
    final String representation;

    TokenType(boolean isIdentifier, String representation) {
        this.isIdentifier = isIdentifier;
        this.representation = representation;
    }
}
