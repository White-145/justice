package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class LocationValue implements Value {
    private final double x;
    private final double y;
    private final double z;
    private final double yaw;
    private final double pitch;

    public LocationValue(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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

    public boolean hasRotation() {
        return yaw != 0 || pitch != 0;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    @Override
    public ValueType getType() {
        return ValueType.LOCATION;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("location{ ");
        Value.writeNumber(writer, x);
        writer.write(", ");
        Value.writeNumber(writer, y);
        writer.write(", ");
        Value.writeNumber(writer, z);
        if (hasRotation()) {
            writer.write(", ");
            Value.writeNumber(writer, yaw);
            writer.write(", ");
            Value.writeNumber(writer, pitch);
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("x");
        writer.value(x);
        writer.name("y");
        writer.value(y);
        writer.name("z");
        writer.value(z);
        writer.name("yaw");
        writer.value(yaw);
        writer.name("pitch");
        writer.value(pitch);
        writer.endObject();
    }
}
