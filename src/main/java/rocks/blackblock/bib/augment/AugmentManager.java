package rocks.blackblock.bib.augment;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.util.BibData;
import rocks.blackblock.bib.util.BibLog;
import rocks.blackblock.bib.util.BibServer;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that handles the saving of augments
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal
public class AugmentManager<C extends Augment> {

    private static final Pattern PER_CHUNK_ZONE_FILE_PATTERN = Pattern.compile("(-?\\d+)x(-?\\d+)\\.nbt");
    public static boolean INITIALIZED = false;
    protected final AugmentKey<C> augment_key;

    /**
     * Save as many augment instances that we know of.
     * (Not all augments keep track of their instances, so those might be saved elsewhere)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void saveAll() {
        for (AugmentKey<?> key : Augment.ALL_AUGMENTS.keySet()) {
            try {
                key.saveAll();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to save augment instances of " + key.getId());
            }
        }
    }

    /**
     * Startup
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    public static void initialize() {
        startGlobalAugments();

        ServerChunkEvents.CHUNK_UNLOAD.register(AugmentManager::unloadChunkAugments);

        INITIALIZED = true;
    }

    /**
     * Start all the global augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    public static void startGlobalAugments() {

        // Start the global augments
        for (AugmentKey.Global<?> key : Augment.Global.REGISTRY.keySet()) {
            try {
                key.get();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to start global augment instances of " + key.getId());
            }
        }
    }

    /**
     * Handle a player tick
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void tickPlayerAugments(ServerPlayerEntity player) {
        // Tick the player augments
        for (AugmentKey.PerPlayer<?> key : Augment.PerPlayer.TICK_REGISTRY.keySet()) {
            try {
                key.get(player).onTick();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to tick player augment instances of " + key.getId());
            }
        }
    }

    /**
     * Handle a player serialization
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void serializePlayerAugments(ServerPlayerEntity player, NbtCompound target_nbt) {

        NbtCompound player_augments_nbt = null;
        int saves = 0;

        for (AugmentKey.PerPlayer<?> key : Augment.PerPlayer.REGISTRY.keySet()) {

            Augment.PerPlayer instance = key.get(player);

            if (instance == null) {
                continue;
            }

            instance.setDirty(false);

            NbtCompound augment_nbt = instance.writeToNbt(player.getRegistryManager());

            if (augment_nbt == null) {
                continue;
            }

            if (player_augments_nbt == null) {
                player_augments_nbt = new NbtCompound();
            }

            try {
                player_augments_nbt.put(key.getId().toString(), augment_nbt);
                saves++;
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to serialize Player augment " + key.getId() + " for " + player);
            }
        }

        if (saves > 0) {
            target_nbt.put("BlackBlockAugments", player_augments_nbt);
        }
    }

    /**
     * Handle a player deserialization
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void deserializePlayerAugments(ServerPlayerEntity player, NbtCompound source_nbt) {

        // Initialize all the augments
        for (AugmentKey.PerPlayer<?> key : Augment.PerPlayer.REGISTRY.keySet()) {
            key.get(player);
        }

        NbtCompound player_augments_nbt = null;

        if (source_nbt.contains("BlackBlockAugments", NbtElement.COMPOUND_TYPE)) {
            player_augments_nbt = (NbtCompound) source_nbt.get("BlackBlockAugments");
        } else if (source_nbt.contains("BlackBlockComponents", NbtElement.COMPOUND_TYPE)) {
            player_augments_nbt = (NbtCompound) source_nbt.get("BlackBlockComponents");
        }

        if (player_augments_nbt == null) {
            return;
        }

        if (player_augments_nbt.isEmpty()) {
            return;
        }

        // Augments that store their data in the chunk should always be deserialized
        for (AugmentKey.PerPlayer<?> key : Augment.PerPlayer.REGISTRY.keySet()) {

            NbtCompound augment_nbt = player_augments_nbt.getCompound(key.getId().toString());

            if (augment_nbt.isEmpty()) {
                continue;
            }

            try {
                Augment.PerPlayer instance = key.get(player);
                instance.readFromNbt(augment_nbt, player.getRegistryManager());
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to deserialize Player augment " + key.getId() + " for " + player);
            }
        }
    }

    /**
     * Create all the augments for the given world
     *
     * @since    0.2.0
     */
    public static void createWorldAugments(ServerWorld world) {

        Augment.PerWorld.REGISTRY.forEach((key, aClass) -> {
            try {
                key.get(world);
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to create world augment instances of " + key.getId());
            }
        });

        Augment.PerBlock.REGISTRY.forEach((key, aClass) -> {
            initializePerBlockAugment(world, key, aClass);
        });

        Augment.PerChunkZone.REGISTRY.forEach((key, aClass) -> {
            initializePerBlockAugment(world, key, aClass);
        });
    }

    /**
     * Create all the augments for the given world
     *
     * @since    0.2.0
     */
    private static void initializePerBlockAugment(ServerWorld world, AugmentKey.PerBlock<?> key, Class<?> clazz) {

        BibLog.attention("Creating PerBlock augments!!");

        // Get the path to where this world's PerChunkZone info is stored
        Path world_path = key.getAugmentInstancePath(world);

        if (!Files.exists(world_path)) {
            try {
                Files.createDirectories(world_path);
            } catch (IOException e) {
                BibServer.registerThrowable(e, "Failed to create PerChunkZone augment instance of " + key.getId());
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(world_path)) {
            for (Path entry : stream) {

                // Only process files that match the pattern
                Matcher matcher = PER_CHUNK_ZONE_FILE_PATTERN.matcher(entry.getFileName().toString());

                if (matcher.matches()) {
                    // Parse integers from the filename
                    int x = Integer.parseInt(matcher.group(1));
                    int z = Integer.parseInt(matcher.group(2));

                    ChunkPos chunk_pos = new ChunkPos(x, z);

                    key.loadFile(world, chunk_pos, entry);
                }
            }
        } catch (Throwable e) {
            BibServer.registerThrowable(e, "Failed to create PerChunkZone augment instance of " + key.getId());
        }
    }

    /**
     * Handle a world tick
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void tickWorldAugments(ServerWorld world) {

        // Tick the augments first
        Augment.PerWorld.REGISTRY.forEach((key, aClass) -> {
            try {
                key.get(world).onTick();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to tick world augment instances of " + key.getId());
            }
        });

        // Now tick the Ticks.WithChunk instances
        AugmentedTicker.WithChunk.tickWorld(world);

        // And the PerBlock & PerChunkZone augments
        Augment.PerBlock.TICKS_WITH_WORLD.forEach((key, aClass) -> {
            try {
                key.forEach((world1, chunk_pos, block_pos, value) -> {
                    value.onTick();
                });
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to tick PerBlock augment instances of " + key.getId());
            }
        });

        Augment.PerChunkZone.TICKS_WITH_WORLD.forEach((key, aClass) -> {
            try {
                key.forEach((world1, chunk_pos, block_pos, value) -> {
                    value.onTick();
                });
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to tick PerChunkZone augment instances of " + key.getId());
            }
        });
    }

    /**
     * Unload a chunk augment
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void unloadChunkAugments(ServerWorld world, Chunk chunk) {

        Augment.PerChunk.REGISTRY.forEach((key, aClass) -> {

            Augment.PerChunk instance = key.getFromCache(chunk);

            if (instance == null) {
                return;
            }

            try {
                instance.onUnload();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to unload chunk augment instances of " + key.getId());
            }
        });
    }

    /**
     * Handle a chunk serialization
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void serializeChunkAugments(ServerWorld world, Chunk chunk, NbtCompound target_nbt) {

        AtomicInteger saves = new AtomicInteger();
        AtomicReference<NbtCompound> chunk_augments_nbt_ref = new AtomicReference<>();

        Augment.PerChunk.STORED_IN_CHUNK_NBT.forEach((key, aClass) -> {

            Augment.PerChunk instance = key.getFromCache(chunk);

            if (instance == null) {
                return;
            }

            instance.setDirty(false);

            NbtCompound augment_nbt = instance.writeToNbt(world.getRegistryManager());

            if (augment_nbt == null) {
                return;
            }

            if (chunk_augments_nbt_ref.get() == null) {
                chunk_augments_nbt_ref.set(new NbtCompound());
            }

            try {
                chunk_augments_nbt_ref.get().put(key.getId().toString(), augment_nbt);
                saves.getAndIncrement();
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to serialize Chunk augment " + key.getId() + " in " + world + " at " + chunk.getPos());
            }
        });

        if (saves.get() > 0) {
            target_nbt.put("BlackBlockAugments", chunk_augments_nbt_ref.get());
        }
    }

    /**
     * Handle a chunk deserialization
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void deserializeChunkAugments(ServerWorld world, Chunk chunk, NbtCompound source_nbt) {

        NbtCompound chunk_augments_nbt = null;

        if (source_nbt.contains("BlackBlockAugments", NbtElement.COMPOUND_TYPE)) {
            chunk_augments_nbt = (NbtCompound) source_nbt.get("BlackBlockAugments");
        } else if (source_nbt.contains("BlackBlockComponents", NbtElement.COMPOUND_TYPE)) {
            chunk_augments_nbt = (NbtCompound) source_nbt.get("BlackBlockComponents");
        }

        if (chunk_augments_nbt == null) {
            return;
        }

        if (chunk_augments_nbt.isEmpty()) {
            return;
        }

        // Augments that store their data in the chunk should always be deserialized
        for (AugmentKey.PerChunk<?> key : Augment.PerChunk.STORED_IN_CHUNK_NBT.keySet()) {

            NbtCompound augment_nbt = chunk_augments_nbt.getCompound(key.getId().toString());

            if (augment_nbt.isEmpty()) {
                continue;
            }

            try {
                Augment.PerChunk instance = key.get(world, chunk);
                instance.readFromNbt(augment_nbt, world.getRegistryManager());
            } catch (Throwable t) {
                BibServer.registerThrowable(t, "Failed to deserialize Chunk augment " + key.getId() + " in " + world + " at " + chunk.getPos());
            }
        }
    }

    /**
     * Handle an upgraded ProtoChunk
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void handleUpgradedProtoChunk(ProtoChunk proto_chunk, WorldChunk world_chunk) {

        for (AugmentKey.PerChunk<?> key : Augment.PerChunk.REGISTRY.keySet()) {
            key.handleUpgradedProtoChunk(proto_chunk, world_chunk);
        }
    }

    /**
     * Create augments for a proto chunk
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void createProtoChunkAugments(ServerWorld world, Chunk chunk) {
        //BBLog.log("Should create proto chunk components of", chunk, chunk.getPos());
    }

    /**
     * Initialize the augment manager
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public AugmentManager(AugmentKey<C> augment_key) {
        this.augment_key = augment_key;
    }

    /**
     * Save this augment instance to the given file if needed.
     * It will be marked as not dirty afterwards.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean saveToFileIfDirty(@NotNull C instance) {
        if (!instance.isDirty()) {
            return false;
        }

        boolean saved = this.saveToFile(instance);

        if (saved) {
            instance.setDirty(false);
        }

        return saved;
    }

    /**
     * Save this augment instance to the given file.
     * This will not check or alter the dirty flag!
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean saveToFile(@NotNull C instance) {

        Path home_path = this.augment_key.getAugmentHomePath();

        if (home_path == null) {
            return false;
        }

        home_path.toFile().mkdirs();

        File file = this.augment_key.getMainFilePath(instance).toFile();
        return this.saveToFile(file, instance);
    }

    /**
     * Save this augment instance to the given file.
     * This will not check or alter the dirty flag!
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean saveToFile(@NotNull File file, @NotNull C instance) {

        NbtCompound data = instance.writeToNbt(new NbtCompound(), instance.getRegistryManager());

        if (data == null) {
            return false;
        }

        return this.saveToFile(file, data);
    }

    /**
     * Save the given NBT data entry to a file
     *
     * @since    0.2.0
     */
    public boolean saveToFile(@NotNull File file, @NotNull NbtCompound data_to_wrap) {

        NbtCompound nbt_compound = new NbtCompound();
        nbt_compound.put("data", data_to_wrap);
        NbtHelper.putDataVersion(nbt_compound);

        try {
            NbtIo.writeCompressed(nbt_compound, file.toPath());
        } catch (IOException var4) {
            BibServer.registerThrowable(var4, "Could not save augment data");
            return false;
        }

        return true;
    }

    /**
     * Parse the NBT from the given file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public NbtCompound parseNbt(File expected_file) {

        if (expected_file == null) {
            return null;
        }

        if (!expected_file.exists()) {
            return null;
        }

        FileInputStream file_stream = null;
        PushbackInputStream pushback_input_stream = null;
        DataInputStream data_input_stream = null;

        try {
            file_stream = new FileInputStream(expected_file);
            pushback_input_stream = new PushbackInputStream(file_stream, 2);

            NbtCompound nbt_compound;

            if (this.isCompressed(pushback_input_stream)) {
                nbt_compound = BibData.readCompressed(pushback_input_stream);
            } else {
                data_input_stream = new DataInputStream(pushback_input_stream);
                nbt_compound = BibData.read(data_input_stream);
            }

            nbt_compound = BibData.performUpdates(nbt_compound);

            if (nbt_compound != null) {

                if (nbt_compound.contains("data")) {
                    return nbt_compound.getCompound("data");
                } else if (nbt_compound.contains("Data")) {
                    return nbt_compound.getCompound("Data");
                }

                return null;
            }

        } catch (Throwable t) {
            BibServer.registerThrowable(t, "Failed to read augment data from file");
            return null;
        } finally {

            if (file_stream != null) {
                try {
                    file_stream.close();
                } catch (Throwable t) {
                    BibServer.registerThrowable(t, "Failed to close file stream");
                }
            }

            if (pushback_input_stream != null) {
                try {
                    pushback_input_stream.close();
                } catch (Throwable t) {
                    BibServer.registerThrowable(t, "Failed to close pushback input stream");
                }
            }

            if (data_input_stream != null) {
                try {
                    data_input_stream.close();
                } catch (Throwable t) {
                    BibServer.registerThrowable(t, "Failed to close data input stream");
                }
            }
        }

        return null;
    }

    /**
     * Get the NBT data of this augment instance's save file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    instance   The actual instance to read into
     */
    @Nullable
    public NbtCompound getSavedNbt(C instance) {

        GlitchGuru.Transaction transaction = GlitchGuru.startTransaction("AugmentManager#getSavedNbt");
        NbtCompound result = null;

        try {
            result = this.getSavedNbtPrivate(instance);
        } catch (Throwable t) {
            transaction.addThrowable(t);
            throw t;
        } finally {
            transaction.finish();
        }

        return result;
    }

    /**
     * Get the NBT data of this augment instance's save file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    instance   The actual instance to read into
     */
    @Nullable
    private NbtCompound getSavedNbtPrivate(C instance) {

        Path expected_path = this.augment_key.getMainFilePath(instance);
        File expected_file = expected_path.toFile();

        NbtCompound nbt_data = this.parseNbt(expected_file);

        if (nbt_data != null) {
            return nbt_data;
        }

        // If the file doesn't exist, try to read the old file
        Path old_path = this.augment_key.getOldNbtPath(instance);

        if (old_path == null) {
            return null;
        }

        File old_file = old_path.toFile();

        if (!old_file.exists()) {
            return null;
        }

        BibLog.log("Reading old Augment data for", this.augment_key, "at", old_path);

        nbt_data = this.parseNbt(old_file);

        if (nbt_data == null) {
            return null;
        }

        if (nbt_data.contains("cardinal_components")) {
            nbt_data = nbt_data.getCompound("cardinal_components");
        } else if (nbt_data.contains("Data")) {
            // Sometimes "Data" is nested in "data", like in the level.dat file
            nbt_data = nbt_data.getCompound("Data");

            if (nbt_data.contains("cardinal_components")) {
                nbt_data = nbt_data.getCompound("cardinal_components");
            }
        }

        String id = this.augment_key.id.toString();

        if (nbt_data.contains(id)) {
            return nbt_data.getCompound(id);
        }

        return null;
    }

    /**
     * Try to read the augment data from a file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    instance   The actual instance to read into
     */
    public boolean readFromFile(C instance) {

        NbtCompound nbt_data = this.getSavedNbt(instance);

        if (nbt_data == null) {
            return false;
        }

        instance.readFromNbt(nbt_data, instance.getRegistryManager());

        return true;
    }

    /**
     * Is the given input stream compressed?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private boolean isCompressed(PushbackInputStream stream) throws IOException {
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = stream.read(bs, 0, 2);
        if (i == 2) {
            int j = (bs[1] & 255) << 8 | bs[0] & 255;
            if (j == 35615) {
                bl = true;
            }
        }

        if (i != 0) {
            stream.unread(bs, 0, i);
        }

        return bl;
    }
}
