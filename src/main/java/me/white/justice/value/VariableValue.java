package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.lexer.Lexer;

import java.io.IOException;

public class VariableValue implements Value {
    private final VariableScope scope;
    private final String name;

    public VariableValue(VariableScope scope, String name) {
        this.scope = scope;
        this.name = name;
    }

    public VariableScope getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.VARIABLE;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.name("variable");
        writer.value(name);
        writer.name("scope");
        writer.value(scope.name);
    }

    @Override
    public String toString() {
        String identifier = identifierToString(name, scope != VariableScope.LOCAL);
        if (scope != VariableScope.LOCAL) {
            return scope.prefix + identifier;
        }
        return identifier;
    }

    public static String identifierToString(String identifier, boolean initialQuoted) {
        boolean quoted = initialQuoted;
        if (identifier.isEmpty() || !Lexer.isLiteralStart(identifier.charAt(0))) {
            quoted = true;
        } else {
            for (char ch : identifier.toCharArray()) {
                if (!Lexer.isLiteral(ch)) {
                    quoted = true;
                    break;
                }
            }
        }
        if (quoted) {
            return "`" + identifier.replace("\n", "\\n").replace("\\", "\\\\") + "`";
        }
        return identifier;
    }
}
