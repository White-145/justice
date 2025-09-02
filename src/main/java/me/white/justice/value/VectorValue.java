package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class VectorValue implements Value {
    private final double x;
    private final double y;
    private final double z;

    public VectorValue(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("vector{ ");
        Value.writeNumber(writer, x);
        writer.write(", ");
        Value.writeNumber(writer, y);
        writer.write(", ");
        Value.writeNumber(writer, z);
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.VECTOR.getName());
        writer.name("x");
        writer.value(x);
        writer.name("y");
        writer.value(y);
        writer.name("z");
        writer.value(z);
        writer.endObject();
    }
}
