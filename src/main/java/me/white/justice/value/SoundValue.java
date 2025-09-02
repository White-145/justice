package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class SoundValue implements Value {
    private final String name;
    private final double volume;
    private final double pitch;
    private final String source;
    private final String variant;

    public SoundValue(String name, double volume, double pitch, String source, String variant) {
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
        this.source = source;
        this.variant = variant;
    }

    public String getName() {
        return name;
    }

    public double getVolume() {
        return volume;
    }

    public double getPitch() {
        return pitch;
    }

    public boolean hasSource() {
        return source != null;
    }

    public String getSource() {
        return source;
    }

    public boolean hasVariant() {
        return variant != null;
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("sound{ ");
        Value.writeString(writer, name);
        if (volume != 1) {
            writer.write(", volume=");
            Value.writeNumber(writer, volume);
        }
        if (pitch != 1) {
            writer.write(", pitch=");
            Value.writeNumber(writer, pitch);
        }
        if (hasSource()) {
            writer.write(", source=\"");
            writer.write(source);
            writer.write("\"");
        }
        if (hasVariant()) {
            writer.write(", variant=\"");
            writer.write(variant);
            writer.write("\"");
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.SOUND.getName());
        writer.name("sound");
        writer.value(name);
        writer.name("pitch");
        writer.value(pitch);
        writer.name("volume");
        writer.value(volume);
        if (hasVariant()) {
            writer.name("variation");
            writer.value(variant);
        }
        if (hasSource()) {
            writer.name("source");
            writer.value(source);
        }
        writer.endObject();
    }
}
