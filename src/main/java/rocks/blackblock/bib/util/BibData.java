package rocks.blackblock.bib.util;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.interfaces.BlackblockDataFixerEntrypoint;
import rocks.blackblock.bib.mixin.NbtListReadViewMixin;
import rocks.blackblock.bib.mixin.NbtListWriteViewMixin;
import rocks.blackblock.bib.mixin.NbtReadViewMixin;
import rocks.blackblock.bib.mixin.NbtWriteViewMixin;
import rocks.blackblock.bib.mixin.dfu.DataFixerBuilderAccessor;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.io.DataInput;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Library class for working with NBT and other types of data
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibData {

    private static DataFixerBuilder DATA_FIXER_BUILDER = null;
    private static boolean DATA_FIXER_OPEN = false;
    private static List<Runnable> DATA_FIXER_SCHEMA_GET_QUEUE = null;
    private static final Int2ObjectOpenHashMap<Map<DSL.TypeReference, List<CustomFixer>>> CUSTOM_FIXERS = new Int2ObjectOpenHashMap<>();

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
     * Set the data-fixer-builder (called via the SchemasMixin)
     */
    @ApiStatus.Internal
    public static void setDataFixerBuilder(@NotNull DataFixerBuilder builder, boolean open) {
        DATA_FIXER_BUILDER = builder;
        DATA_FIXER_OPEN = open;

        if (open) {

            if (DATA_FIXER_SCHEMA_GET_QUEUE != null) {
                for (Runnable runnable : DATA_FIXER_SCHEMA_GET_QUEUE) {
                    runnable.run();
                }
                DATA_FIXER_SCHEMA_GET_QUEUE = null;
            }

            List<BlackblockDataFixerEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("blackblock-data-fixers", BlackblockDataFixerEntrypoint.class);

            for (BlackblockDataFixerEntrypoint entrypointEntry : entrypoints) {
                entrypointEntry.registerDataFixer(builder);
            }
        }
    }

    /**
     * Something is being updated, check for our custom data-fixers
     */
    @ApiStatus.Internal
    public static <T> void handleTriggeredUpdate(DSL.TypeReference type, Dynamic<T> input, int old_version, int new_version) {
        for (int version = old_version; version <= new_version; version++) {
            handleTriggeredUpdate(type, input, version);
        }
    }

    /**
     * Handle a specific version update
     */
    private static <T> void handleTriggeredUpdate(DSL.TypeReference type, Dynamic<T> input, int version) {

        Map<DSL.TypeReference, List<CustomFixer>> fixers = CUSTOM_FIXERS.get(version);

        if (fixers == null) {
            return;
        }

        List<CustomFixer> fixersForType = fixers.get(type);

        if (fixersForType == null) {
            return;
        }

        for (CustomFixer fixer : fixersForType) {
            fixer.performFix(input);
        }
    }

    /**
     * Decode an NBT element
     *
     * @since 0.4.0
     */
    public static <T> Optional<T> decode(Codec<T> codec, NbtElement element) {

        var decoded = codec.decode(BibServer.getDynamicRegistry().getOps(NbtOps.INSTANCE), element);

        if (decoded.isError()) {
            return Optional.empty();
        }

        return Optional.of(decoded.getOrThrow().getFirst());
    }

    /**
     * Encode to an NBT element
     *
     * @since 0.4.0
     */
    public static <T> NbtElement encode(Codec<T> codec, T input) {

        if (input == null) {
            return null;
        }

        return codec.encodeStart(BibServer.getDynamicRegistry().getOps(NbtOps.INSTANCE), input).getOrThrow();
    }

    /**
     * Get a property from the NbtCompound but only if the type matches
     *
     * @since    0.3.0
     */
    public static NbtElement getPropertyOfType(NbtCompound target, String key, byte expectedType) {

        NbtElement value = target.get(key);

        if (value == null || value.getType() != expectedType) {
            return null;
        }

        return value;
    }

    /**
     * See if the NBTCompount contains a property of the given type
     *
     * @since    0.3.0
     */
    public static <T extends NbtElement> T getPropertyOfType(NbtCompound target, String key, NbtType<T> expectedType) {

        NbtElement value = target.get(key);

        if (value == null || value.getNbtType() != expectedType) {
            return null;
        }

        return (T) value;
    }

    /**
     * See if the NBTCompound contains a property of the given type
     *
     * @since    0.3.0
     */
    public static boolean contains(NbtCompound target, String key, byte expectedType) {
        NbtElement value = getPropertyOfType(target, key, expectedType);
        return value != null;
    }

    /**
     * See if the NBTCompound contains a property of the given type
     *
     * @since    0.3.0
     */
    public static boolean contains(NbtCompound target, String key, NbtType<?> expectedType) {
        NbtElement value = getPropertyOfType(target, key, expectedType);
        return value != null;
    }

    /**
     * See if the NBTCompound contains a property that is a UUID
     *
     * @since    0.4.0
     */
    public static boolean containsUuid(NbtCompound target, String key) {
        try {
            UUID result = getUuid(target, key);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get a uuid from a compound
     *
     * @since    0.4.0
     */
    public static UUID getUuid(NbtCompound target, String key) {

        var element = target.get(key);

        if (element == null) {
            return null;
        }

        NbtString stringValue = getPropertyOfType(target, key, NbtString.TYPE);

        if (stringValue != null && stringValue.value() != null) {
            String value = stringValue.value();
            UUID result = null;

            try {
                // Java's UUID methods require dashes
                if (value.length() == 32) {
                    BigInteger bi1 = new BigInteger(value.substring(0, 16), 16);
                    BigInteger bi2 = new BigInteger(value.substring(16, 32), 16);
                    result = new UUID(bi1.longValue(), bi2.longValue());
                } else {
                    result = UUID.fromString(stringValue.value());
                }
            } catch (Exception e) {
                GlitchGuru.registerThrowable(e, "Failed to parse UUID");
            }

            return result;
        }

        // Probably an int-stream
        var result = decode(Uuids.INT_STREAM_CODEC, element);

        return result.orElse(null);
    }

    /**
     * Put a uuid in a compound
     *
     * @since    0.4.0
     */
    public static void putUuid(NbtCompound target, String key, UUID value) {
        var element = encode(Uuids.INT_STREAM_CODEC, value);
        target.put(key, element);
    }

    /**
     * Get a string from a compound
     *
     * @since    0.3.0
     */
    public static String getString(NbtCompound target, String key) {

        NbtString value = getPropertyOfType(target, key, NbtString.TYPE);

        if (value == null) {
            return null;
        }

        return value.value();
    }

    /**
     * Get a list without checking the type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtList getList(NbtCompound compound, String key) {
        return BibData.getPropertyOfType(compound, key, NbtList.TYPE);
    }

    /**
     * Get a list that contains the given type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtList getListContainingType(NbtCompound compound, String key, NbtType<?> expectedType) {
        NbtList list = BibData.getPropertyOfType(compound, key, NbtList.TYPE);

        if (list == null || list.isEmpty()) {
            return list;
        }

        for (NbtElement element : list) {
            if (element.getNbtType() != expectedType) {
                return null;
            }
        }

        return list;
    }

    /**
     * Get a list that contains the given type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtList getListOfCompounds(NbtCompound compound, String key) {
        return BibData.getListContainingType(compound, key, NbtCompound.TYPE);
    }

    /**
     * Get a compound
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound getCompound(NbtCompound compound, String key) {
        return BibData.getPropertyOfType(compound, key, NbtCompound.TYPE);
    }

    /**
     * Get a compound or create it
     *
     * @since    0.4.0
     */
    public static NbtCompound getOrCreateCompound(NbtCompound compound, String key) {
        NbtCompound data = BibData.getPropertyOfType(compound, key, NbtCompound.TYPE);

        if (data == null) {
            data = new NbtCompound();
            compound.put(key, data);
        }

        return data;
    }

    /**
     * Perform DataFixers on the given NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound performUpdates(NbtCompound input) {

        int saved_data_version = NbtHelper.getDataVersion(input, 1343);
        int current_data_version = SharedConstants.getGameVersion().dataVersion().id();

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

        var namespace = BibData.getPropertyOfType(data, "namespace", NbtString.TYPE);

        if (namespace == null) {
            return null;
        }

        var path = BibData.getPropertyOfType(data, "path", NbtString.TYPE);

        if (path == null) {
            return null;
        }

        return Identifier.of(namespace.value(), path.value());
    }

    /**
     * Get the builder & schema for a specific version
     *
     * @since    0.3.0
     */
    private static void getDataFixerSchemaForAdding(int version, BiConsumer<DataFixerBuilder, Schema> consumer) {

        if (DATA_FIXER_BUILDER == null) {
            if (DATA_FIXER_SCHEMA_GET_QUEUE == null) {
                DATA_FIXER_SCHEMA_GET_QUEUE = new ArrayList<>();
            }

            DATA_FIXER_SCHEMA_GET_QUEUE.add(() -> {
                getDataFixerSchemaForAdding(version, consumer);
            });

            return;
        }

        if (!DATA_FIXER_OPEN) {
            throw new RuntimeException("The DataFixers have already been built, unable to add a new schema for '" + version + "'");
        }

        var schemas = ((DataFixerBuilderAccessor) DATA_FIXER_BUILDER).bb$getSchemas();
        var schema = schemas.get(version);

        if (schema == null) {
            schema = DATA_FIXER_BUILDER.addSchema(version, Schema::new);
        }

        consumer.accept(DATA_FIXER_BUILDER, schema);
    }

    /**
     * Add a fixer for the given version
     *
     * @since    0.3.0
     */
    public static void addDataFixer(int version, Function<Schema, DataFix> fixer_creator) {
        getDataFixerSchemaForAdding(version, (builder, schema) -> {
            builder.addFixer(fixer_creator.apply(schema));
        });
    }

    /**
     * Add a very simple "DataFixer"-like consumer for a specific version
     * that can directly work on the root Dynamic value
     *
     * @since    0.3.0
     */
    public static void addDataFixer(int version, DSL.TypeReference type, String name, Consumer<Dynamic<?>> consumer) {
        var version_fixers = CUSTOM_FIXERS.computeIfAbsent(version, v -> new Object2ObjectOpenHashMap<>());
        var type_fixers = version_fixers.computeIfAbsent(type, t -> new ArrayList<>());
        type_fixers.add(new CustomFixer(name, consumer));
    }

    /**
     * Create an empty NbtWriteView
     */
    public static NbtWriteView createNbtWriteView(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries) {
        return NbtWriteView.create(reporter, registries);
    }

    /**
     * Create an NbtWriteView with the given NBT compound as the target
     */
    public static NbtWriteView createNbtWriteView(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound target) {
        return NbtWriteViewMixin.bb$createNbtWriteView(reporter, registries.getOps(NbtOps.INSTANCE), target);
    }

    /**
     * Create an NbtWriteView with the given NBT compound as the target
     */
    public static NbtWriteView createNbtWriteView(NbtCompound target) {
        var reporter = BibLog.createErrorReporter();
        return NbtWriteViewMixin.bb$createNbtWriteView(reporter, BibServer.getDynamicRegistry().getOps(NbtOps.INSTANCE), target);
    }

    /**
     * Create an NbtReadView with the given NBT compound as the source
     */
    public static NbtReadView createNbtReadView(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound source) {
        return (NbtReadView) NbtReadView.create(reporter, registries, source);
    }

    /**
     * Create a NbtReadView with the given NBT compound as the source
     */
    public static NbtReadView createNbtReadView(NbtCompound source) {
        var reporter = BibLog.createErrorReporter();
        var registries = BibServer.getDynamicRegistry();
        return (NbtReadView) NbtReadView.create(reporter, registries, source);
    }

    /**
     * Get the NBTCompound of a ReadView
     */
    public static NbtCompound extractCompound(ReadView view) {
        if (view instanceof NbtReadView nbtView) {
            return extractCompound(nbtView);
        }

        return null;
    }

    /**
     * Get the NBTCompound of a ReadView
     */
    public static NbtCompound extractCompound(NbtReadView nbtView) {
        return ((NbtReadViewMixin) nbtView).bb$getNbt();
    }

    /**
     * Get the NBTCompound of a ReadView
     */
    public static NbtCompound extractCompound(WriteView view) {
        if (view instanceof NbtWriteView nbtView) {
            return extractCompound(nbtView);
        }

        return null;
    }

    /**
     * Get the NBTCompound of a ReadView
     */
    public static NbtCompound extractCompound(NbtWriteView nbtView) {
        return ((NbtWriteViewMixin) nbtView).bb$getNbt();
    }

    /**
     * Get the list of a ReadListView
     */
    public static List<NbtCompound> extractList(ReadView.ListReadView view) {

        if (view instanceof NbtReadView.NbtListReadView nbtView) {
            return extractList(nbtView);
        }

        return null;
    }

    /**
     * Get the underling list of an NbtListReadView
     */
    public static List<NbtCompound> extractList(NbtReadView.NbtListReadView view) {
        return ((NbtListReadViewMixin) view).bb$getListOfCompounds();
    }

    /**
     * Get the list of a WriteView.ListView
     */
    public static NbtList extractList(WriteView.ListView view) {

        if (view instanceof NbtWriteView.NbtListView nbtView) {
            return extractList(nbtView);
        }

        return null;
    }

    /**
     * Get the underlying NbtList instance
     */
    public static NbtList extractList(NbtWriteView.NbtListView view) {
        return ((NbtListWriteViewMixin) view).bb$getNbtList();
    }

    private record CustomFixer(String name, Consumer<Dynamic<?>> consumer) {
        public void performFix(Dynamic<?> input) {
            try {
                consumer.accept(input);
            } catch (Throwable t) {
                GlitchGuru.registerThrowable(t, "Failed to perform custom datafixer '" + name + "'");
            }
        }
    }
}
