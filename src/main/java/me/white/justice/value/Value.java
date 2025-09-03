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
            writeEnclosed(writer, name, "`");
        } else {
            writer.write(name);
        }
    }

    static void writeEnclosed(Writer writer, String string, String close) throws IOException {
        writer.write(close);
        writer.write(string.replace("\\", "\\\\").replace(close, "\\" + close).replace("\n", "\\n"));
        writer.write(close);
    }

    static void writeNumber(Writer writer, double number) throws IOException {
        writer.write(Double.toString((double)Math.round(number * 1000) / 1000));
    }

    void write(Writer writer) throws IOException;

    void writeJson(JsonWriter writer) throws IOException;
}
