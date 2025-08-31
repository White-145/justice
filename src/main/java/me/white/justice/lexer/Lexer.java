package me.white.justice.lexer;

import me.white.justice.CompilationException;

import java.util.Stack;

public class Lexer {
    private final Stack<Token> pending = new Stack<>();
    private final String buffer;
    private Token eofToken = null;
    private int pos = -1;
    private int row = 0;
    private int column = 0;

    public Lexer(String buffer) {
        this.buffer = buffer;
        advance();
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
        if (!pending.isEmpty()) {
            return pending.getFirst().getPos();
        }
        return pos;
    }

    public boolean canRead() {
        return pos < buffer.length();
    }

    public void advance() {
        if (!canRead()) {
            return;
        }
        if (pos != -1 && buffer.charAt(pos) == '\n') {
            row += 1;
            column = 0;
        }
        pos += 1;
        column += 1;
    }

    public void advanceReader(int i) {
        if (!pending.isEmpty()) {
            Token first = pending.getFirst();
            pos = first.getPos();
            row = first.getRow();
            column = first.getColumn();
            pending.clear();
        }
        if (!canRead()) {
            return;
        }
        int end = pos;
        while ((end = buffer.indexOf('\n', end, pos + i) + 1) != 0) {
            row += 1;
            column = 0;
        }
        pos += i;
        column += i;
    }

    public void skipWhitespace() {
        while (canRead() && isWhitespace(buffer.charAt(pos))) {
            advance();
            // skip comments
            if (pos + 2 < buffer.length() && buffer.charAt(pos) == '/' && buffer.charAt(pos + 1) == '/') {
                pos = buffer.indexOf('\n', pos) + 1;
                row += 1;
                column = 0;
            }
        }
    }

    private String readIdentifier() throws CompilationException {
        skipWhitespace();
        if (!canRead() || (!isLiteralStart(buffer.charAt(pos)) && buffer.charAt(pos) != '`')) {
            throw new CompilationException("No identifier");
        }
        if (buffer.charAt(pos) == '`') {
            advance();
            StringBuilder builder = new StringBuilder();
            while (canRead() && buffer.charAt(pos) != '`') {
                char ch = buffer.charAt(pos);
                if (ch == '\\') {
                    advance();
                    if (!canRead()) {
                        throw new CompilationException("Incomplete escape sequence");
                    }
                    ch = buffer.charAt(pos);
                    if (ch != '`' && ch != '\\') {
                        throw new CompilationException("Invalid escape sequence");
                    }
                }
                builder.append(ch);
                advance();
            }
            if (!canRead() || buffer.charAt(pos) != '`') {
                throw new CompilationException("Incomplete identifier");
            }
            advance();
            return builder.toString();
        }
        int start = pos;
        while (canRead() && isLiteral(buffer.charAt(pos))) {
            pos += 1;
            column += 1;
        }
        return buffer.substring(start, pos);
    }

    private String readString() throws CompilationException {
        skipWhitespace();
        if (!canRead() || buffer.charAt(pos) != '"') {
            throw new CompilationException("No string");
        }
        advance();
        StringBuilder builder = new StringBuilder();
        while (canRead() && buffer.charAt(pos) != '"') {
            char ch = buffer.charAt(pos);
            if (ch == '\\') {
                advance();
                if (!canRead()) {
                    throw new CompilationException("Incomplete escape sequence");
                }
                ch = switch (buffer.charAt(pos)) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> throw new CompilationException("Invalid escape sequence");
                };
            }
            builder.append(ch);
            advance();
        }
        if (!canRead() || buffer.charAt(pos) != '"') {
            throw new CompilationException("Incomplete string");
        }
        advance();
        return builder.toString();
    }

    private double readNumber() throws CompilationException {
        skipWhitespace();
        if (!canRead() || !isNumberStart(buffer.charAt(pos))) {
            throw new CompilationException("No number");
        }
        int start = pos;
        char ch = buffer.charAt(pos);
        if (ch == '-' || ch == '+') {
            advance();
        }
        boolean hasDot = false;
        while (canRead()) {
            ch = buffer.charAt(pos);
            if (ch == '.') {
                if (hasDot) {
                    break;
                }
                hasDot = true;
            } else if (ch == 'e' || ch == 'E') {
                advance();
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
            advance();
        }
        try {
            return Double.parseDouble(buffer.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new CompilationException("Invalid number: " + e.getMessage());
        }
    }

    private String readPlaceholder() throws CompilationException {
        int start = pos;
        int depth = 0;
        advance();
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
            advance();
        }
        if (!canRead()) {
            throw new CompilationException("Incomplete placeholder");
        }
        advance();
        return buffer.substring(start, pos);
    }

    private int readColor() throws CompilationException {
        int color = 0;
        advance();
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
            advance();
        }
        return color;
    }

    private String readEnum() throws CompilationException {
        skipWhitespace();
        if (!canRead() || buffer.charAt(pos) != '\'') {
            throw new CompilationException("No enum");
        }
        advance();
        StringBuilder builder = new StringBuilder();
        while (canRead() && buffer.charAt(pos) != '\'') {
            char ch = buffer.charAt(pos);
            if (ch == '\\') {
                advance();
                if (!canRead()) {
                    throw new CompilationException("Incomplete escape sequence");
                }
                ch = switch (buffer.charAt(pos)) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '\'' -> '\'';
                    case '\\' -> '\\';
                    default -> throw new CompilationException("Invalid escape sequence");
                };
            }
            builder.append(ch);
            advance();
        }
        if (!canRead() || buffer.charAt(pos) != '\'') {
            throw new CompilationException("Incomplete enum");
        }
        advance();
        return builder.toString();
    }

    private Token eof() {
        if (eofToken == null) {
            eofToken = new Token(TokenType.EOF, null, pos, row, column);
        }
        return eofToken;
    }

    private Token tokenSingle(TokenType type, int pos, int row, int column) {
        advance();
        return new Token(type, null, pos, row, column);
    }

    private Token next() throws CompilationException {
        skipWhitespace();
        if (!canRead()) {
            return eof();
        }
        int pos = this.pos;
        int row = this.row;
        int column = this.column;
        return switch (buffer.charAt(pos)) {
            case '{' -> tokenSingle(TokenType.BLOCK_OPEN, pos, row, column);
            case '}' -> tokenSingle(TokenType.BLOCK_CLOSE, pos, row, column);
            case '(' -> tokenSingle(TokenType.ARGS_OPEN, pos, row, column);
            case ')' -> tokenSingle(TokenType.ARGS_CLOSE, pos, row, column);
            case '<' -> tokenSingle(TokenType.SELECTOR_OPEN, pos, row, column);
            case '>' -> tokenSingle(TokenType.SELECTOR_CLOSE, pos, row, column);
            case ',' -> tokenSingle(TokenType.COMMA, pos, row, column);
            case '=' -> tokenSingle(TokenType.EQUALS, pos, row, column);
            case ';' -> tokenSingle(TokenType.EOL, pos, row, column);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.' -> new Token(TokenType.NUMBER, readNumber(), pos, row, column);
            case '%' -> new Token(TokenType.PLACEHOLDER, readPlaceholder(), pos, row, column);
            case '#' -> new Token(TokenType.COLOR, readColor(), pos, row, column);
            case '\'' -> new Token(TokenType.ENUM, readEnum(), pos, row, column);
            case '"' -> new Token(TokenType.STRING, readString(), pos, row, column);
            case '`' -> new Token(TokenType.IDENTIFIER, readIdentifier(), pos, row, column);
            default -> new Token(TokenType.LITERAL, readIdentifier(), pos, row, column);
        };
    }

    public boolean canLex() throws CompilationException {
        return !pending.isEmpty() || !peek().isOf(TokenType.EOF);
    }

    public Token peek(int offset) throws CompilationException {
        if (offset < pending.size()) {
            return pending.get(offset);
        }
        for (int i = pending.size(); i < offset; ++i) {
            Token token = next();
            if (token.isOf(TokenType.EOF)) {
                return token;
            }
            pending.add(token);
        }
        Token token = next();
        if (!token.isOf(TokenType.EOF)) {
            pending.add(token);
        }
        return token;
    }

    public Token peek() throws CompilationException {
        return peek(0);
    }

    public Token read() throws CompilationException {
        if (!pending.isEmpty()) {
            return pending.pop();
        }
        return next();
    }

    public void skip(int n) throws CompilationException {
        if (pending.size() > n) {
            for (int i = n; i < pending.size(); ++i) {
                pending.pop();
            }
            return;
        }
        n -= pending.size();
        pending.clear();
        for (int i = 0; i < n; ++i) {
            if (next().isOf(TokenType.EOF)) {
                break;
            }
        }
    }

    public boolean guess(TokenType type, boolean skip) throws CompilationException {
        boolean guessed = peek().isOf(type);
        if (guessed && skip) {
            skip(1);
        }
        return guessed;
    }

    public boolean guessIdentifier(boolean skip) throws CompilationException {
        boolean guessed = peek().isIdentifier();
        if (guessed && skip) {
            skip(1);
        }
        return guessed;
    }

    public Token expect(TokenType type) throws CompilationException {
        Token token = peek();
        if (!token.isOf(type)) {
            throw new CompilationException("Expected " + type.representation + ", got " + token.getType().representation);
        }
        return read();
    }

    public Token expectIdentifier() throws CompilationException {
        Token token = peek();
        if (!token.isIdentifier()) {
            throw new CompilationException("Expected identifier, got " + token.getType().representation);
        }
        return read();
    }
}
