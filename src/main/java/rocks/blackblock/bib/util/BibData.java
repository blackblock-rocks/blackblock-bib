package rocks.blackblock.bib.util;

import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;

import java.io.DataInput;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Library class for working with NBT and other types of data
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibData {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibData() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get a list without checking the type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtList getList(NbtCompound compound, String key) {

        NbtElement element = compound.get(key);

        if (element == null) {
            return null;
        }

        return (NbtList) element;
    }

    /**
     * Perform DataFixers on the given NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound performUpdates(NbtCompound input) {

        int saved_data_version = NbtHelper.getDataVersion(input, 1343);
        int current_data_version = SharedConstants.getGameVersion().getSaveVersion().getId();

        NbtCompound result = DataFixTypes.SAVED_DATA_COMMAND_STORAGE.update(
                Schemas.getFixer(),
                input,
                saved_data_version,
                current_data_version
        );

        return result;
    }

    /**
     * Read a compressed NBT from a data stream
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound readCompressed(PushbackInputStream input_stream) throws IOException {
        return NbtIo.readCompressed(input_stream, NbtSizeTracker.ofUnlimitedBytes());
    }

    /**
     * Read a compressed NBT from a file path
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound readCompressed(Path path) throws IOException {
        return NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
    }

    /**
     * Read an NBT from a data stream
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound read(DataInput input) throws IOException {
        return NbtIo.readCompound(input);
    }

    /**
     * Write an NBT to the given file, but compressed
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void writeCompressed(NbtCompound nbt, Path file) throws IOException {
        NbtIo.writeCompressed(nbt, file);
    }

    /**
     * Write an NBT to the given file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void write(NbtCompound nbt, Path file) throws IOException {
        NbtIo.write(nbt, file);
    }

    /**
     * Copy over the data, overwriting existing properties
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void assign(NbtCompound target, NbtCompound source) {

        if (source == null || source.isEmpty()) {
            return;
        }

        for (String key : source.getKeys()) {
            target.put(key, source.get(key));
        }
    }

    /**
     * Copy over the data, but only non-existing properties
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void assignDefaults(NbtCompound target, NbtCompound source) {

        if (source == null || source.isEmpty()) {
            return;
        }

        for (String key : source.getKeys()) {

            if (target.contains(key)) {
                continue;
            }

            target.put(key, source.get(key));
        }
    }

    /**
     * Combine 2 sets into a new HashSet
     *
     * @since    0.2.0
     */
    @SafeVarargs
    public static <T> Set<T> combineSets(Set<T> ...sets) {

        var result = new HashSet<T>();

        for (var set : sets) {
            if (set != null) {
                result.addAll(set);
            }
        }

        return result;
    }

    /**
     * Serialize an identifier to NBT
     *
     * @since    0.2.0
     */
    public static NbtCompound serialize(Identifier id) {

        if (id == null) {
            return null;
        }

        NbtCompound result = new NbtCompound();
        result.putString("namespace", id.getNamespace());
        result.putString("path", id.getPath());

        return result;
    }

    /**
     * Parse an identifier
     *
     * @since    0.2.0
     */
    public static Identifier parseToIdentifier(NbtElement element) {

        if (!(element instanceof NbtCompound data)) {
            return null;
        }

        if (!data.contains("namespace", NbtElement.STRING_TYPE) || !data.contains("path", NbtElement.STRING_TYPE)) {
            return null;
        }

        String namespace = data.getString("namespace");
        String path = data.getString("path");

        return Identifier.of(namespace, path);
    }
}
