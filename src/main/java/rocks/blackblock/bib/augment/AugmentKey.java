package rocks.blackblock.bib.augment;


import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.collection.WeakValueHashMap;
import rocks.blackblock.bib.collection.WorldChunkBlockMap;
import rocks.blackblock.bib.util.*;

import java.nio.file.Path;
import java.util.*;

/**
 * A key for augments
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public abstract class AugmentKey<$C extends Augment> {

    protected final Identifier id;
    protected final Class<$C> augment_class;
    protected final AugmentManager<$C> manager;

    /**
     * Initialize the augment key
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public AugmentKey(Identifier id, Class<$C> augment_class) {
        this.augment_class = augment_class;
        this.id = id;
        this.manager = new AugmentManager<>(this);
    }

    /**
     * Return the identifier of this augment key
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public final Identifier getId() {
        return this.id;
    }

    /**
     * Get the augment class of this key's augment type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public final Class<$C> getAugmentClass() {
        return this.augment_class;
    }

    /**
     * Return the directory path where to save this augment instance data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    abstract protected Path getAugmentInstancePath($C instance);

    /**
     * Return the name of the main file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    abstract protected String getMainFileName($C instance);

    /**
     * Save all the instances of this augment to file if needed.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    abstract public boolean saveAll();

    /**
     * Save the given instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean save($C instance) {
        return this.manager.saveToFile(instance);
    }

    /**
     * Get the path to the main file of this augment instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public Path getMainFilePath($C instance) {
        return this.getAugmentHomePath().resolve(this.getMainFileName(instance));
    }

    /**
     * Return the directory path where to save this augment's data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected Path getAugmentHomePath() {
        Path main_path = getAugmentRootPath();
        return main_path.resolve(this.id.toUnderscoreSeparatedString());
    }

    /**
     * Get the possible old path of this augment instance.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public Path getOldNbtPath($C instance) {
        return null;
    }

    /**
     * Return a string representation
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public String toString() {
        return BibLog.createArg(this).add("id", this.id).add("class", this.augment_class.getSimpleName()).toString();
    }

    /**
     * Return the root path where all augment data lives
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Path getAugmentRootPath() {
        Path main_path = BibServer.getMainWorldDirectory();
        return main_path.resolve("blackblock-augments");
    }

    /**
     * A key where the instance can be fetched by a UUID
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public abstract static class AugmentKeyByUUID<C extends Augment> extends AugmentKey<C> {

        /**
         * Initialize the augment key
         *
         * @since   0.1.0
         */
        public AugmentKeyByUUID(Identifier id, Class<C> augment_class) {
            super(id, augment_class);
        }

        /**
         * Get the augment by its UUID
         *
         * @since   0.1.0
         */
        public abstract C get(UUID uuid);
    }

    /**
     * A key for global augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Global<C extends Augment.Global> extends AugmentKey<C> {

        // Global augments have only 1 instance
        private C instance = null;

        // The instantiator
        private final Augment.Global.Instantiator instantiator;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Global(Identifier id, Class<C> augment_class, @NotNull Augment.Global.Instantiator instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
        }

        /**
         * Get the possible old path of this augment instance.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Path getOldNbtPath(C instance) {
            return getAugmentRootPath().resolve("old").resolve("level.dat");
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {

            // It's possible that the instance hasn't been created yet
            if (this.instance == null) {
                return false;
            }

            return this.save(this.instance);
        }

        /**
         * Return the directory path where to save this augment instance data.
         * (Globals store their data in their home path)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return this.getAugmentHomePath();
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return this.id.toUnderscoreSeparatedString() + ".nbt";
        }

        /**
         * Get the instance of this augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get() {

            if (this.instance != null) {
                return this.instance;
            }

            if (BibLog.DEBUG) {
                BibLog.log("Creating new Global augment instance", this.id);
            }

            this.instance = (C) this.instantiator.create();

            if (BibLog.DEBUG) {
                BibLog.log("Created new Global augment instance", this.instance);
            }

            this.manager.readFromFile(this.instance);

            if (BibLog.DEBUG) {
                BibLog.log("Returning new Global augment instance", this.instance);
            }

            return this.instance;
        }
    }

    /**
     * A key for world augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class PerWorld<C extends Augment.PerWorld> extends AugmentKey<C> {

        // World augments have 1 instance per world
        private final WeakHashMap<World, C> cache = new WeakHashMap<>();

        // The instantiator
        private final Augment.PerWorld.Instantiator instantiator;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public PerWorld(Identifier id, Class<C> augment_class, Augment.PerWorld.Instantiator instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
        }

        /**
         * Get the possible old path of this augment instance.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Path getOldNbtPath(C instance) {

            World world = instance.getWorld();

            RegistryKey<World> world_ref = world.getRegistryKey();

            if (world_ref == World.OVERWORLD) {
                return getAugmentRootPath().resolve("old").resolve("world").resolve("cardinal_world_components.dat");
            } else if (world_ref == World.NETHER) {
                return getAugmentRootPath().resolve("old").resolve("DIM-1").resolve("cardinal_world_components.dat");
            } else if (world_ref == World.END) {
                return getAugmentRootPath().resolve("old").resolve("DIM1").resolve("cardinal_world_components.dat");
            }

            return null;
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {
            for (C instance : this.cache.values()) {
                this.save(instance);
            }

            return true;
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            net.minecraft.world.World world = instance.getWorld();
            return world.getRegistryKey().getValue().toUnderscoreSeparatedString() + ".nbt";
        }

        /**
         * Return the directory path where to save this augment instance data.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return this.getAugmentHomePath().resolve(this.getMainFileName(instance));
        }

        /**
         * Get the instance of this augment for the given world
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(World world) {

            C result = this.cache.get(world);

            if (result != null) {
                return result;
            }

            if (BibLog.DEBUG) {
                BibLog.log("Creating new World augment instance", this.id, "for", world);
            }

            result = (C) this.instantiator.create(world);

            if (BibLog.DEBUG) {
                BibLog.log("Created new World augment instance", result);
            }

            this.manager.readFromFile(result);

            this.cache.put(world, result);

            if (BibLog.DEBUG) {
                BibLog.log("Returning new World augment instance", result);
            }

            return result;
        }
    }

    /**
     * A key for chunk augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class PerChunk<C extends Augment.PerChunk> extends AugmentKey<C> {

        // Chunk augments have 1 instance per chunk
        // Though when the chunk is loaded, it's a ProtoChunk. Afterwards a normal Chunk is created.
        private final WeakHashMap<Chunk, C> cache = new WeakHashMap<>();

        // The instantiator
        private final Augment.PerChunk.Instantiator<C> instantiator;

        // Should this augment's data be stored inside the Chunk's nbt?
        private final boolean store_in_chunk_nbt;

        // Should this augment be ticked with the chunk?
        private final boolean tick_with_chunk;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public PerChunk(Identifier id, Class<C> augment_class, boolean store_in_chunk_nbt, boolean tick_with_chunk, Augment.PerChunk.Instantiator<C> instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
            this.store_in_chunk_nbt = store_in_chunk_nbt;
            this.tick_with_chunk = tick_with_chunk;
        }

        /**
         * Should this augment's data be stored inside the Chunk's nbt?
         * By default, this is false, and the data will be stored in a separate file.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public boolean storeInChunkNbt() {
            return this.store_in_chunk_nbt;
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {

            // If the data is stored in the chunk nbt, we don't need to save anything
            if (this.storeInChunkNbt()) {
                return false;
            }

            for (C instance : this.cache.values()) {
                this.save(instance);
            }

            return true;
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return null;
        }

        /**
         * Return the directory path where to save this augment instance data.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return null;
        }

        /**
         * Get an the instance of this augment for the given chunk,
         * but only from cache. Do not create a new one
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C getFromCache(Chunk chunk) {

            C result = this.cache.get(chunk);

            if (result != null) {
                return result;
            }

            if (chunk instanceof WrapperProtoChunk roc) {
                chunk = roc.getWrappedChunk();

                if (chunk == null) {
                    return null;
                }

                result = this.cache.get(chunk);
            }

            return result;
        }

        /**
         * Get an the instance of this augment for the given chunk,
         * but only from cache. Do not create a new one
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C getFromCache(ServerWorld world, ChunkPos chunk_pos) {

            Chunk chunk = world.getChunk(chunk_pos.x, chunk_pos.z, ChunkStatus.EMPTY);

            if (chunk == null) {
                return null;
            }

            return this.getFromCache(chunk);
        }

        /**
         * Get or create the instance of this augment for the given chunk
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(ServerWorld world, Chunk chunk) {

            C result = this.getFromCache(chunk);

            if (result != null) {
                return result;
            }

            // Get rid of those nasty ReadOnlyChunks
            if (chunk instanceof WrapperProtoChunk roc) {
                chunk = roc.getWrappedChunk();
            }

            result = this.instantiator.create(world, chunk);

            if (!this.store_in_chunk_nbt) {
                this.manager.readFromFile(result);
            }

            this.cache.put(chunk, result);

            return result;
        }

        /**
         * Get or create the instance of this augment for the given chunk
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(ServerWorld world, ChunkPos chunk_pos) {
            Chunk chunk = world.getChunk(chunk_pos.x, chunk_pos.z, ChunkStatus.EMPTY);
            return this.get(world, chunk);
        }

        /**
         * Get or create the instance of this augment for the given chunk
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(Chunk chunk) {

            C result = this.getFromCache(chunk);

            if (result != null) {
                return result;
            }

            return null;
        }

        /**
         * Handle the upgrade of a proto chunk to a world chunk.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void handleUpgradedProtoChunk(ProtoChunk proto_chunk, WorldChunk world_chunk) {

            C instance = this.getFromCache(proto_chunk);

            if (instance == null) {
                return;
            }

            cache.put(world_chunk, instance);
            instance.onUpgrade(proto_chunk, world_chunk);

            if (instance.isDirty()) {
                instance.markDirty();
            }
        }
    }

    /**
     * A key for player augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class PerPlayer<C extends Augment.PerPlayer> extends AugmentKeyByUUID<C> {

        // The cache of instances
        private final WeakHashMap<ServerPlayerEntity, C> cache = new WeakHashMap<>();

        // The instantiator
        private final Augment.PerPlayer.Instantiator<C> instantiator;

        // Should it tick?
        private final boolean do_tick;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public PerPlayer(Identifier id, Class<C> augment_class, boolean do_tick, Augment.PerPlayer.Instantiator<C> instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
            this.do_tick = do_tick;
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {
            return false;
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return null;
        }

        /**
         * Return the directory path where to save this augment instance data.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return null;
        }

        /**
         * Get or create the instance of this augment for the given UUID
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(UUID uuid) {

            ServerPlayerEntity player = BibServer.getServer().getPlayerManager().getPlayer(uuid);

            if (player == null) {
                return null;
            }

            return this.get(player);
        }

        /**
         * Get or create the instance of this augment for the given player
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(ServerPlayerEntity player) {

            C result = this.cache.get(player);

            if (result != null) {
                return result;
            }

            result = this.instantiator.create(player);

            this.cache.put(player, result);

            return result;
        }
    }

    /**
     * A key for UUID augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class PerUUID<C extends Augment.PerUUID> extends AugmentKeyByUUID<C> {

        // The cache of instances
        private final WeakValueHashMap<UUID, C> cache = new WeakValueHashMap<>();

        // A queue of instances that need to be saved
        private final Map<UUID, C> save_queue = new HashMap<>();

        // The instantiator
        private final Augment.PerUUID.Instantiator<C> instantiator;

        // Should it tick?
        private final boolean do_tick;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public PerUUID(Identifier id, Class<C> augment_class, boolean do_tick, Augment.PerUUID.Instantiator<C> instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
            this.do_tick = do_tick;
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return instance.getUUID().toString() + ".nbt";
        }

        /**
         * Return the directory path where to save this augment instance data.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return this.getAugmentHomePath().resolve(this.getMainFileName(instance));
        }

        /**
         * Queue an instance to be saved
         * (This is needed to ensure an instance doesn't get garbage collected before a save is triggered)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        protected void queueSave(C instance) {
            this.cache.put(instance.getUUID(), instance);
            this.save_queue.put(instance.getUUID(), instance);
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {

            // Save all the ones that are in the queue
            for (C instance : this.save_queue.values()) {
                this.save(instance);
            }

            // Clear the queue
            this.save_queue.clear();

            // Save all the ones that are in the cache
            for (C instance : this.cache.values()) {

                // There is no need to re-save PerUUID augments that are not dirty:
                // the previous save is safe.
                if (!instance.isDirty()) {
                    continue;
                }

                this.save(instance);
            }

            return true;
        }

        /**
         * Get or create the instance of this augment for the given UUID
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public C get(UUID uuid) {

            C result = this.cache.get(uuid);

            if (result != null) {
                return result;
            }

            result = this.instantiator.create(this, uuid);

            this.manager.readFromFile(result);

            this.cache.put(uuid, result);

            return result;
        }
    }

    /**
     * A key for PerItemStack augments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class PerItemStack<C extends Augment.PerItemStack, T extends ItemConvertible> extends AugmentKey<C> {

        // The cache of instances
        private final WeakValueHashMap<ItemStack, C> cache = new WeakValueHashMap<>();

        // The instantiator
        private final Augment.PerItemStack.Instantiator<C, T> instantiator;

        // The stack is allowed check
        private final Augment.PerItemStack.StackIsAllowedCheck stack_is_allowed_check;

        // Should it tick?
        private final boolean do_tick;

        // The string id to use in the NBT
        private final String nbt_id;

        /**
         * Initialize the augment key
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public PerItemStack(Identifier id, Class<C> augment_class, Identifier nbt_namespace_id, boolean do_tick, Augment.PerItemStack.StackIsAllowedCheck check, Augment.PerItemStack.Instantiator<C, T> instantiator) {
            super(id, augment_class);
            this.stack_is_allowed_check = check;
            this.instantiator = instantiator;
            this.do_tick = do_tick;
            this.nbt_id = nbt_namespace_id.getNamespace() + ":" + nbt_namespace_id.getPath();
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        public boolean saveAll() {
            return false;
        }

        /**
         * Get the main name of the file
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return null;
        }

        /**
         * Return the directory path where to save this augment instance data.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return null;
        }

        /**
         * Get or create the container NBT
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public NbtCompound getContainerNbt(ItemStack stack) {

            if (stack == null) {
                return null;
            }

            NbtCompound root = BibItem.getOrCreateCustomNbt(stack);

            NbtCompound container;

            if (root.contains(this.nbt_id, NbtElement.COMPOUND_TYPE)) {
                container = root.getCompound(this.nbt_id);
            } else {
                container = new NbtCompound();
                root.put(this.nbt_id, container);
            }

            return container;
        }

        /**
         * Get or create the instance of this augment for the given UUID
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Nullable
        public C get(ItemStack stack) {

            if (stack == null || stack.isEmpty()) {
                return null;
            }

            if (!this.stack_is_allowed_check.isAllowed(stack)) {
                return null;
            }

            C result = this.cache.get(stack);

            if (result != null) {
                return result;
            }

            NbtCompound container = this.getContainerNbt(stack);

            if (container == null) {
                return null;
            }

            result = this.instantiator.create(this, (T) stack.getItem(), stack, container);

            this.cache.put(stack, result);

            return result;
        }
    }

    /**
     * A key for per-block augments
     *
     * @since    0.2.0
     */
    public static class PerBlock<C extends Augment.PerBlock> extends AugmentKey<C> {

        // Instances per world & chunk
        protected final WorldChunkBlockMap<C> cache = new WorldChunkBlockMap<>(new WeakHashMap<>(10));

        // The instantiator
        protected final Augment.PerBlock.Instantiator<C> instantiator;

        // Should this augment be ticked?
        private final boolean tick_with_world;

        /**
         * Initialize the augment key
         *
         * @since    0.2.0
         */
        public PerBlock(Identifier id, Class<C> augment_class, boolean tick_with_world, Augment.PerBlock.Instantiator<C> instantiator) {
            super(id, augment_class);
            this.instantiator = instantiator;
            this.tick_with_world = tick_with_world;
        }

        /**
         * Get the main name of the file:
         * Always returns null PerChunkZone.
         *
         * @since    0.2.0
         */
        @Override
        protected String getMainFileName(C instance) {
            return null;
        }

        /**
         * Return the directory path where to save this augment instance data:
         * Always returns null PerChunkZone.
         *
         * @since    0.2.0
         */
        @Override
        protected Path getAugmentInstancePath(C instance) {
            return null;
        }

        /**
         * Get the main name of the file of the given chunk
         *
         * @since    0.2.0
         */
        protected String getMainFileName(ChunkPos chunk_pos) {
            String result = String.format("%04d", chunk_pos.x) + "x" + String.format("%04d", chunk_pos.z) + ".nbt";
            return result;
        }

        /**
         * Return the directory path where to save this augment of the given world
         *
         * @since    0.2.0
         */
        protected Path getAugmentInstancePath(World world) {
            Identifier id = world.getRegistryKey().getValue();
            return this.getAugmentHomePath().resolve(id.toUnderscoreSeparatedString());
        }

        /**
         * Save all the instances of this augment to file if needed.
         *
         * @since    0.2.0
         */
        @Override
        public boolean saveAll() {

            this.cache.forEachChunk(this::writeFileIfDirty);

            return true;
        }

        /**
         * Write all the origins to the given chunk file
         * if any of the instances are dirty
         *
         * @since    0.2.0
         */
        public boolean writeFileIfDirty(World world, ChunkPos chunk_pos, Map<BlockPos, C> instances) {

            boolean is_dirty = false;
            for (C instance : instances.values()) {
                if (instance.isDirty()) {
                    is_dirty = true;
                }
            }

            if (!is_dirty) {
                return false;
            }

            Path world_dir = this.getAugmentInstancePath(world);
            String chunk_filename = this.getMainFileName(chunk_pos);
            Path chunk_path = world_dir.resolve(chunk_filename);

            return this.writeFile(world, chunk_pos, chunk_path, instances);
        }

        /**
         * Write all the origins to the given chunk file
         *
         * @since    0.2.0
         */
        public boolean writeFile(World world, ChunkPos chunk_pos, Path path, Map<BlockPos, C> instances) {

            NbtList list = new NbtList();

            for (C instance : instances.values()) {
                NbtCompound data = instance.writeToNbt(new NbtCompound(), instance.getRegistryManager());

                if (data == null) {
                    continue;
                }

                var origin = BibPos.serializeBlockPos(instance.getOrigin());
                data.put("origin", origin);

                list.add(data);
            }

            if (list.isEmpty()) {
                return false;
            }

            NbtCompound compound = new NbtCompound();
            compound.put("origins", list);

            return this.manager.saveToFile(path.toFile(), compound);
        }

        /**
         * Load all the origins of the given chunk file
         *
         * @since    0.2.0
         */
        public boolean loadFile(ServerWorld world, ChunkPos chunk_pos, Path path) {

            NbtCompound nbt = this.manager.parseNbt(path.toFile());

            if (nbt == null) {
                return false;
            }

            NbtList origin_list = nbt.getList("origins", NbtElement.COMPOUND_TYPE);

            for (NbtElement element : origin_list) {
                NbtCompound data = (NbtCompound) element;

                NbtElement origin = data.get("origin");

                if (origin == null) {
                    continue;
                }

                BlockPos origin_pos = BibPos.parseBlockPos(origin);

                if (origin_pos == null) {
                    continue;
                }

                C instance = this.get(world, origin_pos);

                instance.readFromNbt(data, world.getRegistryManager());
            }

            return true;
        }

        /**
         * Iterate over all the instances in the given world
         *
         * @since    0.2.0
         */
        public void forEach(WorldChunkBlockMap.QuadrupleIterator<C> iterator) {
            this.cache.forEach(iterator);
        }

        /**
         * Get or create the instance of this augment for the given world and origin.
         *
         * @since    0.2.0
         */
        public C get(World world, BlockPos origin) {

            C instance = this.cache.get(world, origin);

            if (instance == null) {
                // Create a new instance
                instance = this.instantiator.create(world, origin);
                this.cache.put(world, origin, instance);
                BibLog.log(" -- CREATED:", instance, "at", origin);
            }

            return instance;
        }
    }

    /**
     * A key for chunk zone augments
     *
     * @since    0.2.0
     */
    public static class PerChunkZone<C extends Augment.PerChunkZone> extends PerBlock<C> {

        /**
         * Initialize the augment key
         *
         * @since    0.2.0
         */
        public PerChunkZone(Identifier id, Class<C> augment_class, boolean tick_with_world, Augment.PerChunkZone.Instantiator<C> instantiator) {
            super(id, augment_class, tick_with_world, instantiator);
        }

        /**
         * Is the given chunk affected by any of this augment?
         * (Will not check any BlockPos)
         *
         * @since    0.2.0
         */
        public boolean affectsChunk(World world, ChunkPos chunkPos) {

            Map<BlockPos, C> block_map = this.cache.get(world, chunkPos);

            if (block_map != null) {
                for (C instance : block_map.values()) {
                    if (instance.affects(chunkPos)) {
                        return true;
                    }
                }
                return true;
            }

            var instances = this.cache.valuesPerWorld(world);

            if (instances == null) {
                return false;
            }

            for (var instance : instances) {
                if (instance.affects(chunkPos)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Is the given block position affected by any of this augment?
         *
         * @since    0.2.0
         */
        public boolean affectsBlock(World world, BlockPos blockPos) {

            ChunkPos chunk_pos = new ChunkPos(blockPos);

            if (!this.affectsChunk(world, chunk_pos)) {
                return false;
            }

            var instances = this.cache.valuesPerWorld(world);

            if (instances == null) {
                return false;
            }

            for (var value : instances) {
                if (value.affects(blockPos)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Get all chunk zones affecting a specific chunk.
         *
         * @since    0.2.0
         */
        public List<C> getInstancesAffectingChunk(World world, ChunkPos chunkPos) {

            var chunk_values = this.cache.valuesPerWorld(world);

            if (chunk_values == null || chunk_values.isEmpty()) {
                return Collections.emptyList();
            }

            List<C> result = new ArrayList<>(chunk_values.size());

            for (var instance : chunk_values) {
                if (instance.affects(chunkPos)) {
                    result.add(instance);
                }
            }

            return result;
        }
    }
}