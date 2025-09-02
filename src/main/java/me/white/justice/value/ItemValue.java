package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.LongBuffer;
import java.util.Base64;
import java.util.Map;

public class ItemValue implements Value {
    private final CompoundTag tag;

    public ItemValue(CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag getTag() {
        return tag;
    }

    private static void writeTag(Writer writer, Tag<?> tag) throws IOException {
        switch (tag) {
            case ArrayTag<?> array -> {
                LongBuffer buffer = LongBuffer.allocate(array.length());
                String prefix;
                String suffix;
                if (array instanceof ByteArrayTag byteArray) {
                    prefix = "B";
                    suffix = "B";
                    for (byte value : byteArray.getValue()) {
                        buffer.put(value);
                    }
                } else if (array instanceof IntArrayTag intArray) {
                    prefix = "I";
                    suffix = "";
                    for (int value : intArray.getValue()) {
                        buffer.put(value);
                    }
                } else if (array instanceof LongArrayTag longArray) {
                    prefix = "L";
                    suffix = "L";
                    buffer.put(((LongArrayTag)array).getValue());
                } else {
                    throw new AssertionError("unreachable");
                }
                buffer.flip();
                writer.write("[");
                writer.write(prefix);
                writer.write(";");
                if (buffer.limit() != 0) {
                    writer.write(" ");
                }
                for (int i = 0; i < buffer.limit(); ++i) {
                    writer.write(Long.toString(buffer.get()));
                    writer.write(suffix);
                    if (i != buffer.limit() - 1) {
                        writer.write(", ");
                    }
                }
                writer.write("]");
            }
            case CompoundTag compound -> {
                writer.write("{");
                int i = 0;
                for (Map.Entry<String, Tag<?>> entry : compound.entrySet()) {
                    Value.writeString(writer, entry.getKey());
                    writer.write(": ");
                    writeTag(writer, entry.getValue());
                    i += 1;
                    if (i != compound.entrySet().size()) {
                        writer.write(", ");
                    }
                }
                writer.write("}");
            }
            case ListTag<?> list -> {
                writer.write("[");
                for (int i = 0; i < list.size(); ++i) {
                    writeTag(writer, list.get(i));
                    if (i != list.size() - 1) {
                        writer.write(", ");
                    }
                }
                writer.write("]");
            }
            default -> {
                writer.write(tag.valueToString());
            }
        }
    }

    @Override
    public ValueType getType() {
        return ValueType.ITEM;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("item");
        writeTag(writer, tag);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        byte[] bytes = new NBTSerializer(true).toBytes(new NamedTag(null, tag));
        String serialized = Base64.getEncoder().encodeToString(bytes);
        writer.name("item");
        writer.value(serialized);
        writer.endObject();
    }
}
