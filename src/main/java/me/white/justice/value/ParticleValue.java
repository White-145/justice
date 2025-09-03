package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public class ParticleValue implements Value {
    private final String name;
    @Nullable
    private final String material;
    private final double spreadH;
    private final double spreadV;
    private final double motionX;
    private final double motionY;
    private final double motionZ;
    private final int count;
    private final int color;
    private final double size;

    public ParticleValue(String name, String material, double spreadH, double spreadV, double motionX, double motionY, double motionZ, int count, int color, double size) {
        this.name = name;
        this.material = material;
        this.spreadH = spreadH;
        this.spreadV = spreadV;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.count = count;
        this.color = color;
        this.size = size;
    }

    public boolean hasMaterial() {
        return material != null;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getMaterial() {
        return material;
    }

    public boolean hasSpread() {
        return spreadH != 0 || spreadV != 0;
    }

    public double getSpreadH() {
        return spreadH;
    }

    public double getSpreadV() {
        return spreadV;
    }

    public boolean hasMotion() {
        return motionX != 0 || motionY != 0 || motionZ != 0;
    }

    public double getMotionX() {
        return motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public int getCount() {
        return count;
    }

    public int getColor() {
        return color;
    }

    public double getSize() {
        return size;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("particle{ ");
        Value.writeEnclosed(writer, name, "\"");
        if (hasMaterial()) {
            writer.write(", material=\"");
            writer.write(material);
            writer.write("\"");
        }
        if (hasSpread()) {
            writer.write(", spread={ ");
            Value.writeNumber(writer, spreadH);
            writer.write(", ");
            Value.writeNumber(writer, spreadV);
            writer.write(" }");
        }
        if (hasMotion()) {
            writer.write(", motion={ ");
            Value.writeNumber(writer, motionX);
            writer.write(", ");
            Value.writeNumber(writer, motionY);
            writer.write(", ");
            Value.writeNumber(writer, motionZ);
            writer.write(" }");
        }
        if (count != 1) {
            writer.write(", count=");
            writer.write(Integer.toString(count));
        }
        if (color != 0xFF0000) {
            writer.write(String.format(", color=#%06X", color));
        }
        if (size != 1) {
            writer.write(", size=");
            Value.writeNumber(writer, size);
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.NUMBER.getName());
        writer.name("particle_type");
        writer.value(name);
        if (hasMaterial()) {
            writer.name("material");
            writer.value(material);
        }
        writer.name("count");
        writer.value(count);
        writer.name("first_spread");
        writer.value(spreadH);
        writer.name("second_spread");
        writer.value(spreadV);
        writer.name("x_motion");
        writer.value(motionX);
        writer.name("y_motion");
        writer.value(motionY);
        writer.name("z_motion");
        writer.value(motionZ);
        writer.name("color");
        writer.value(color);
        writer.name("size");
        writer.value(size);
        writer.endObject();
    }
}
