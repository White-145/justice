package me.white.justice.value;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public class PotionValue implements Value {
    private final String name;
    private final int amplifier;
    private final int duration;

    public PotionValue(String name, int amplifier, int duration) {
        this.name = name;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public ValueType getType() {
        return ValueType.POTION;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("potion{ ");
        Value.writeString(writer, name);
        if (amplifier != 0) {
            writer.write(", amplifier=");
            writer.write(Integer.toString(amplifier));
        }
        if (duration >= 0) {
            writer.write(", duration=");
            writer.write(Integer.toString(duration));
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("potion");
        writer.value(name);
        writer.name("amplifier");
        writer.value(amplifier);
        writer.name("duration");
        writer.value(duration);
        writer.endObject();
    }
}
