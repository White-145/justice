package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.lexer.Lexer;

import java.io.IOException;
import java.io.Writer;

public interface Value {
    static void writeIdentifier(Writer writer, String name, boolean force) throws IOException {
        if (force || name.isEmpty() || !Lexer.isLiteralStart(name.charAt(0))) {
            force = true;
        } else {
            for (char ch : name.toCharArray()) {
                if (!Lexer.isLiteral(ch)) {
                    force = true;
                    break;
                }
            }
        }
        if (force) {
            writer.write("`" + name.replace("\n", "\\n").replace("\\", "\\\\").replace("`", "\\`") + "`");
        } else {
            writer.write(name);
        }
    }

    static void writeString(Writer writer, String string) throws IOException {
        writer.write("\"");
        writer.write(string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t"));
        writer.write("\"");
    }

    static void writeNumber(Writer writer, double number) throws IOException {
        writer.write(Double.toString((double)Math.round(number * 1000) / 1000));
    }

    ValueType getType();

    void write(Writer writer) throws IOException;

    void writeJson(JsonWriter writer) throws IOException;
}
