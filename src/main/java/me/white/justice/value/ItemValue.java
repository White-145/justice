package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.*;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
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

    // since net.querz.nbt does not provide adequate customization
    private static void writeTag(Writer writer, Tag<?> tag) throws IOException {
        switch (tag) {
            case ArrayTag<?> array -> {
                String prefix = "";
                String suffix = "";
                if (array instanceof ByteArrayTag) {
                    prefix = "B";
                    suffix = "B";
                } else if (array instanceof IntArrayTag) {
                    prefix = "I";
                } else if (array instanceof LongArrayTag) {
                    prefix = "L";
                    suffix = "L";
                }
                writer.write("[");
                writer.write(prefix);
                writer.write(";");
                writer.write(" ");
                for (int i = 0; i < array.length(); ++i) {
                    writer.write(Array.get(array.getValue(), i).toString());
                    writer.write(suffix);
                    if (i != array.length() - 1) {
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
    public void write(Writer writer) throws IOException {
        writer.write("item");
        writeTag(writer, tag);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(ValueType.ITEM.getName());
        byte[] bytes = new NBTSerializer(true).toBytes(new NamedTag(null, tag));
        String serialized = Base64.getEncoder().encodeToString(bytes);
        writer.name("item");
        writer.value(serialized);
        writer.endObject();
    }
}
