package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.Base64;

public class ItemValue implements Value {
    private final CompoundTag item;

    public ItemValue(CompoundTag item) {
        this.item = item;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        Tag<?> tag;
        SNBTParser parser = new SNBTParser(lexer.getBuffer().substring(lexer.getPos()));
        try {
            tag = parser.parse(Tag.DEFAULT_MAX_DEPTH, true);
        } catch (ParseException e) {
            throw new CompilationException("Invalid item data: " + e.getMessage());
        }
        if (!(tag instanceof CompoundTag)) {
            throw new CompilationException("Invalid item data");
        }
        lexer.advanceReader(parser.getReadChars() - 1);
        return new ItemValue((CompoundTag)tag);
    }

    public CompoundTag getItem() {
        return item;
    }

    @Override
    public ValueType getType() {
        return ValueType.ITEM;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        byte[] bytes = new NBTSerializer(true).toBytes(new NamedTag(null, item));
        String serialized = Base64.getEncoder().encodeToString(bytes);
        writer.name("item");
        writer.value(serialized);
    }

    @Override
    public String toString() {
        return item.valueToString();
    }
}
