package me.white.justice.lexer;

import me.white.justice.CompilationException;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private static final Map<Character, TokenType> SIMPLE_TOKENS = new HashMap<>();
    private final String buffer;
    private Token pending;
    private Token eof = null;
    private int pos = -1;
    private int row = 0;
    private int column = 0;

    static {
        SIMPLE_TOKENS.put('{', TokenType.BLOCK_OPEN);
        SIMPLE_TOKENS.put('}', TokenType.BLOCK_CLOSE);
        SIMPLE_TOKENS.put('(', TokenType.ARGS_OPEN);
        SIMPLE_TOKENS.put(')', TokenType.ARGS_CLOSE);
        SIMPLE_TOKENS.put('<', TokenType.SELECTOR_OPEN);
        SIMPLE_TOKENS.put('>', TokenType.SELECTOR_CLOSE);
        SIMPLE_TOKENS.put(',', TokenType.COMMA);
        SIMPLE_TOKENS.put('=', TokenType.EQUALS);
        SIMPLE_TOKENS.put(';', TokenType.EOL);
    }

    public Lexer(String buffer) {
        this.buffer = buffer.replace("\r\n", "\n");
        advance(1);
    }

    public static boolean isLiteralStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    public static boolean isLiteral(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_';
    }

    public static boolean isNumberStart(char ch) {
        return (ch >= '0' && ch <= '9') || ch == '-' || ch == '+' || ch == '.';
    }

    public static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    public String getBuffer() {
        return buffer;
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

    public int getReadPos() {
        if (pending != null) {
            return pending.getPos();
        }
        return pos;
    }

    public int getReadRow() {
        if (pending != null) {
            return pending.getRow();
        }
        return pos;
    }

    public int getReadColumn() {
        if (pending != null) {
            return pending.getColumn();
        }
        return column;
    }

    public void advance(int advance) {
        pos += advance;
    }

    public void advanceFrom(int start) {
        int advance = pos - start;
        pos = start;
        advance(advance);
    }

    private boolean canRead() {
        return pos < buffer.length();
    }

    public boolean hasNext() {
        if (pending != null) {
            return true;
        }
        return canRead();
    }

    private void skipWhitespace() {
        if (!canRead()) {
            return;
        }
        int start = pos;
        while (canRead() && isWhitespace(buffer.charAt(pos))) {
            pos += 1;
        }
        advanceFrom(start);
    }

    private String readEnclosed(char close, String name) throws CompilationException {
        int start = pos;
        pos += 1;
        StringBuilder builder = new StringBuilder();
        while (canRead() && buffer.charAt(pos) != close) {
            char ch = buffer.charAt(pos);
            if (ch == '\n') {
                break;
            }
            if (ch == '\\') {
                pos += 1;
                if (!canRead()) {
                    throw new CompilationException("Incomplete escape sequence");
                }
                ch = buffer.charAt(pos);
                if (ch == '\n') {
                    pos += 1;
                    continue;
                }
                if (ch != close && ch != '\\') {
                    throw new CompilationException("Invalid escape sequence");
                }
            }
            builder.append(ch);
            pos += 1;
        }
        if (!canRead() || buffer.charAt(pos) != close) {
            throw new CompilationException("Incomplete " + name);
        }
        pos += 1;
        advanceFrom(start);
        return builder.toString();
    }

    private String readLiteral() {
        int start = pos;
        while (canRead() && isLiteral(buffer.charAt(pos))) {
            pos += 1;
        }
        String literal = buffer.substring(start, pos);
        advanceFrom(start);
        return literal;
    }

    private double readNumber() throws CompilationException {
        int start = pos;
        char ch = buffer.charAt(pos);
        if (ch == '-' || ch == '+') {
            pos += 1;
        }
        boolean hasDot = false;
        boolean hasE = false;
        while (canRead()) {
            ch = buffer.charAt(pos);
            if (ch == '.') {
                if (hasDot || hasE) {
                    break;
                }
                hasDot = true;
            } else if (ch == 'e' || ch == 'E') {
                if (hasE) {
                    break;
                }
                hasE = true;
                pos += 1;
                if (!canRead()) {
                    throw new CompilationException("Incomplete number");
                }
                ch = buffer.charAt(pos);
                if (ch != '-' && ch != '+' && (ch < '0' || ch > '9')) {
                    break;
                }
            } else if (ch < '0' || ch > '9') {
                break;
            }
            pos += 1;
        }
        try {
            double number = Double.parseDouble(buffer.substring(start, pos));
            int advance = pos - start;
            pos = start;
            advance(advance);
            return number;
        } catch (NumberFormatException e) {
            throw new CompilationException("Invalid number: " + e.getMessage());
        }
    }

    private String readPlaceholder() throws CompilationException {
        int start = pos;
        int depth = 0;
        pos += 1;
        while (canRead()) {
            char ch = buffer.charAt(pos);
            if (ch == '(') {
                depth += 1;
            }
            if (ch == ')') {
                depth -= 1;
                if (depth < 0) {
                    throw new CompilationException("Malformed placeholder");
                }
            }
            if ((ch == ')' || ch == '%') && depth == 0) {
                break;
            }
            pos += 1;
        }
        if (!canRead()) {
            throw new CompilationException("Incomplete placeholder");
        }
        pos += 1;
        String placeholder = buffer.substring(start, pos);
        advanceFrom(start);
        return placeholder;
    }

    private int readColor() throws CompilationException {
        int start = pos;
        int color = 0;
        pos += 1;
        for (int i = 0; i < 6; ++i) {
            if (!canRead()) {
                throw new CompilationException("Incomplete color");
            }
            char ch = buffer.charAt(pos);
            color <<= 4;
            if (ch >= '0' && ch <= '9') {
                color += ch - '0';
            } else if (ch >= 'a' && ch <= 'f') {
                color += ch - 'a' + 10;
            } else if (ch >= 'A' && ch <= 'F') {
                color += ch - 'A' + 10;
            } else {
                throw new CompilationException("Invalid color component");
            }
            pos += 1;
        }
        advanceFrom(start);
        return color;
    }

    private Token next() throws CompilationException {
        skipWhitespace();
        if (!canRead()) {
            if (eof == null) {
                eof = new Token(TokenType.EOF, null, pos, row, column);
            }
            return eof;
        }
        int pos = this.pos;
        int row = this.row;
        int column = this.column;
        char ch = buffer.charAt(pos);
        if (SIMPLE_TOKENS.containsKey(ch)) {
            advance(1);
            return new Token(SIMPLE_TOKENS.get(ch), null, pos, row, column);
        }
        if (isNumberStart(ch)) {
            return new Token(TokenType.NUMBER, readNumber(), pos, row, column);
        }
        if (isLiteralStart(ch)) {
            return new Token(TokenType.LITERAL, readLiteral(), pos, row, column);
        }
        Token token = switch (ch) {
            case '"' -> new Token(TokenType.STRING, readEnclosed('"', "string"), pos, row, column);
            case '`' -> new Token(TokenType.IDENTIFIER, readEnclosed('`', "identifier"), pos, row, column);
            case '\'' -> new Token(TokenType.ENUM, readEnclosed('\'', "enum"), pos, row, column);
            case '%' -> new Token(TokenType.PLACEHOLDER, readPlaceholder(), pos, row, column);
            case '#' -> new Token(TokenType.COLOR, readColor(), pos, row, column);
            default -> throw new CompilationException("Invalid token");
        };
        skipWhitespace();
        return token;
    }

    public Token peek() throws CompilationException {
        if (pending == null) {
            pending = next();
        }
        return pending;
    }

    public Token read() throws CompilationException {
        if (pending == null) {
            return next();
        }
        Token token = pending;
        pending = null;
        return token;
    }

    public Token expect(TokenType type) throws CompilationException {
        Token token = peek();
        if (!token.isOf(type)) {
            throw new CompilationException("Expected " + type + ", got " + token.getType());
        }
        return token;
    }

    public Token expectIdentifier() throws CompilationException {
        Token token = peek();
        if (!token.getType().isIdentifier()) {
            throw new CompilationException("Expected identifier-like, got " + token.getType());
        }
        return token;
    }
}
