package rocks.blackblock.bib.augment;


import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.nbt.NbtCompoundProxy;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Things that can be saved
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings({
        // Ignore unused warnings: this is a library after all
        "unused",

        // Ignore warnings of using raw types
        "rawtypes",

        // Ignore unchecked typecast warnings
        "unchecked"
})
public interface Augment {

    // All the registered augments
    Map<AugmentKey<?>, Class<?>> ALL_AUGMENTS = new HashMap<>();

    // Augments that listen to PlayerInventory changes
    Map<AugmentKey.AugmentKeyByUUID<? extends PlayerInventoryChangeListener>, Class<? extends PlayerInventoryChangeListener>> PLAYER_INVENTORY_CHANGE_LISTENERS = new HashMap<>();

    // Augments that want to do a check every second (when the player is not stationary)
    Map<AugmentKey.AugmentKeyByUUID<? extends NonStationaryCheckPerSecond>, Class<? extends NonStationaryCheckPerSecond>> NON_STATIONARY_CHECKS_PER_SECOND = new HashMap<>();

    /**
     * Augments can implement some extra event listener interfaces.
     * Check those here.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static <C extends Augment> void checkExtraInterfaces(AugmentKey<C> key, Class<C> augment_class) {

        if (PlayerInventoryChangeListener.class.isAssignableFrom(augment_class)) {
            PLAYER_INVENTORY_CHANGE_LISTENERS.put((AugmentKey.AugmentKeyByUUID<? extends PlayerInventoryChangeListener>) key, (Class<? extends PlayerInventoryChangeListener>) augment_class);
        }

        if (NonStationaryCheckPerSecond.class.isAssignableFrom(augment_class)) {
            NON_STATIONARY_CHECKS_PER_SECOND.put((AugmentKey.AugmentKeyByUUID<? extends NonStationaryCheckPerSecond>) key, (Class<? extends NonStationaryCheckPerSecond>) augment_class);
        }
    }

    /**
     * Mark this augment instance as dirty
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void markDirty() {
        this.setDirty(true);
    }

    /**
     * Set this augment instance as dirty
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    void setDirty(boolean dirty);

    /**
     * Is this augment instance dirty (needs to be saved)?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    boolean isDirty();

    /**
     * Reads this augment's properties from a {@link NbtCompound}.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    nbt    An {@code NbtCompound} on which this augment's serializable data has been written
     */
    @Contract(mutates = "this")
    void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup);

    /**
     * Writes this augment's properties to a {@link NbtCompound}.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    nbt    An {@code NbtCompound} on which to write this augment's serializable data
     */
    @Contract(mutates = "param")
    NbtCompound writeToNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup);

    /**
     * Writes this augment's properties to a {@link NbtCompound}.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default NbtCompound writeToNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return writeToNbt(new NbtCompound(), registryLookup);
    }

    /**
     * Get the registry manager
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default RegistryWrapper.WrapperLookup getRegistryManager() {
        return BibMod.getDynamicRegistry();
    }

    /**
     * A global augment: only one instance of this augment exists.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface Global extends Augment {

        // All the registered global augments
        Map<AugmentKey.Global<?>, Class<?>> REGISTRY = new HashMap<>();

        /**
         * Register a global augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends Global> AugmentKey.Global<C> register(Identifier id, Class<C> augment_class, Instantiator instantiator) {
            AugmentKey.Global<C> key = new AugmentKey.Global<>(id, augment_class, instantiator);
            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (AugmentManager.INITIALIZED) {
                BibLog.attention("Registered global augment after initialization: " + id);
                key.get();
            }

            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<GC extends Global> {
            GC create();
        }
    }

    /**
     * A world augment: one instance of this augment exists per world/dimension
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface PerWorld extends Augment {

        // All the registered world augments
        Map<AugmentKey.PerWorld<?>, Class<?>> REGISTRY = new HashMap<>();

        /**
         * Register a world augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerWorld> AugmentKey.PerWorld<C> register(Identifier id, Class<C> augment_class, PerWorld.Instantiator instantiator) {
            AugmentKey.PerWorld<C> key = new AugmentKey.PerWorld<>(id, augment_class, instantiator);
            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);
            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<WC extends PerWorld> {
            WC create(World world);
        }

        /**
         * Method to get the world this augment is in.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        World getWorld();

        /**
         * Do something on each tick
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        default void onTick() {}

        /**
         * Get the registry manager from the World instance
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default RegistryWrapper.WrapperLookup getRegistryManager() {
            return this.getWorld().getRegistryManager();
        }
    }

    /**
     * A chunk augment: one instance of this augment can exist per chunk
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface PerChunk extends Augment {

        // All the registered chunk augments
        Map<AugmentKey.PerChunk<?>, Class<?>> REGISTRY = new HashMap<>();

        // All the registered chunk augments that are stored in the chunk's NBT
        Map<AugmentKey.PerChunk<?>, Class<?>> STORED_IN_CHUNK_NBT = new HashMap<>();

        // All the registered chunk augments that are stored in separate files
        Map<AugmentKey.PerChunk<?>, Class<?>> STORED_IN_SEPARATE_FILES = new HashMap<>();

        // All the registered chunk augments that tick along with the chunk
        Map<AugmentKey.PerChunk<?>, Class<?>> TICK_WITH_CHUNK = new HashMap<>();

        /**
         * Register a chunk augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerChunk> AugmentKey.PerChunk<C> register(Identifier id, Class<C> augment_class, PerChunk.Instantiator<C> instantiator) {
            return register(id, augment_class, false, false, instantiator);
        }

        /**
         * Register a chunk augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerChunk> AugmentKey.PerChunk<C> register(Identifier id, Class<C> augment_class, boolean store_in_chunk_nbt, boolean tick_with_chunk, PerChunk.Instantiator<C> instantiator) {
            AugmentKey.PerChunk<C> key = new AugmentKey.PerChunk<>(id, augment_class, store_in_chunk_nbt, tick_with_chunk, instantiator);
            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (store_in_chunk_nbt) {
                STORED_IN_CHUNK_NBT.put(key, augment_class);
            } else {
                STORED_IN_SEPARATE_FILES.put(key, augment_class);
            }

            if (tick_with_chunk) {
                TICK_WITH_CHUNK.put(key, augment_class);
            }

            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<CC extends PerChunk> {
            CC create(ServerWorld world, Chunk chunk);
        }

        /**
         * Mark this augment instance as dirty
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default void markDirty() {

            // Only do this for augments that are stored in the chunk's NBT?
            Chunk chunk = this.getChunk();
            if (chunk != null) {
                chunk.setNeedsSaving(true);
            }

            this.setDirty(true);
        }

        /**
         * Handle chunk upgrades
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        default void onUpgrade(ProtoChunk proto_chunk, WorldChunk world_chunk) {
            this.setChunk(world_chunk);
        }

        /**
         * Handle chunk unloads
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        default void onUnload() {

        }

        /**
         * Method that sets the chunk
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        void setChunk(Chunk chunk);

        /**
         * Method to get the chunk this augment is in.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        Chunk getChunk();

        /**
         * Do something on each tick.
         * (Augment has to be registered as such!)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        default void onTick() {}
    }

    /**
     * A player augment: one instance of this augment can exist per player
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface PerPlayer extends Augment {

        // All the registered player augments
        Map<AugmentKey.PerPlayer<?>, Class<?>> REGISTRY = new HashMap<>();

        // All the registered player augments that should tick
        Map<AugmentKey.PerPlayer<?>, Class<?>> TICK_REGISTRY = new HashMap<>();

        /**
         * Register a chunk augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerPlayer> AugmentKey.PerPlayer<C> register(Identifier id, Class<C> augment_class, boolean do_tick, PerPlayer.Instantiator<C> instantiator) {
            AugmentKey.PerPlayer<C> key = new AugmentKey.PerPlayer<>(id, augment_class, do_tick, instantiator);

            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (do_tick) {
                TICK_REGISTRY.put(key, augment_class);
            }

            checkExtraInterfaces(key, augment_class);

            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<CC extends PerPlayer> {
            CC create(ServerPlayerEntity player);
        }

        /**
         * Implement the dirty methods, but ignore them.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default void setDirty(boolean dirty) {
            // Ignore
        }

        /**
         * Implement the dirty methods, but ignore them.
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default boolean isDirty() {
            return false;
        }

        /**
         * Method to get the player of this augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        ServerPlayerEntity getPlayer();

        /**
         * Do something on each tick
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        default void onTick() {}

        /**
         * Get the registry manager from the Player instance
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default RegistryWrapper.WrapperLookup getRegistryManager() {
            return BibMod.getDynamicRegistry(this.getPlayer());
        }
    }

    /**
     * A UUID augment: one instance of this augment can exist per UUID
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface PerUUID extends Augment {

        // All the registered UUID augments
        Map<AugmentKey.PerUUID<?>, Class<?>> REGISTRY = new HashMap<>();

        // All the registered UUID augments that should tick
        Map<AugmentKey.PerUUID<?>, Class<?>> TICK_REGISTRY = new HashMap<>();

        /**
         * Register a chunk augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerUUID> AugmentKey.PerUUID<C> register(Identifier id, Class<C> augment_class, boolean do_tick, PerUUID.Instantiator<C> instantiator) {
            AugmentKey.PerUUID<C> key = new AugmentKey.PerUUID<>(id, augment_class, do_tick, instantiator);

            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (do_tick) {
                TICK_REGISTRY.put(key, augment_class);
            }

            checkExtraInterfaces(key, augment_class);

            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<CC extends PerUUID> {
            CC create(AugmentKey.PerUUID<CC> key, UUID uuid);
        }

        /**
         * Mark this augment instance as dirty
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default void markDirty() {
            this.getAugmentKey().queueSave(this);
            this.setDirty(true);
        }

        /**
         * Get the augment key this belongs to
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @NotNull
        <C extends PerUUID> AugmentKey.PerUUID<C> getAugmentKey();

        /**
         * Method to get the UUID of this augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        UUID getUUID();
    }

    /**
     * An ItemStack augment: one instance of this augment can exist per ItemStack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface PerItemStack extends Augment, NbtCompoundProxy {

        // All the registered PerItemStack augments
        Map<AugmentKey.PerItemStack<?, ?>, Class<?>> REGISTRY = new HashMap<>();

        // All the registered UUID augments that should tick
        Map<AugmentKey.PerItemStack<?, ?>, Class<?>> TICK_REGISTRY = new HashMap<>();

        /**
         * Register a PerItemStack augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerItemStack, T extends ItemConvertible> AugmentKey.PerItemStack<C, T> register(Identifier id, Class<C> augment_class, boolean do_tick, PerItemStack.StackIsAllowedCheck check, PerItemStack.Instantiator<C, T> instantiator) {
            AugmentKey.PerItemStack<C, T> key = new AugmentKey.PerItemStack<>(id, augment_class, id, do_tick, check, instantiator);

            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (do_tick) {
                TICK_REGISTRY.put(key, augment_class);
            }

            checkExtraInterfaces(key, augment_class);

            return key;
        }

        /**
         * Register a PerItemStack augment
         * with a custom NBT namespace id
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        static <C extends PerItemStack, T extends ItemConvertible> AugmentKey.PerItemStack<C, T> register(Identifier id, Class<C> augment_class, Identifier nbt_namespace, boolean do_tick, PerItemStack.StackIsAllowedCheck check, PerItemStack.Instantiator<C, T> instantiator) {
            AugmentKey.PerItemStack<C, T> key = new AugmentKey.PerItemStack<>(id, augment_class, nbt_namespace, do_tick, check, instantiator);

            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (do_tick) {
                TICK_REGISTRY.put(key, augment_class);
            }

            checkExtraInterfaces(key, augment_class);

            return key;
        }

        /**
         * The functional interface used to create new instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface Instantiator<CC extends PerItemStack, T extends ItemConvertible> {
            CC create(AugmentKey.PerItemStack<CC, T> key, T item, ItemStack item_stack, NbtCompound own_nbt);
        }

        /**
         * The functional interface used to see if an ItemStack is allowed to use this augment
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @FunctionalInterface
        interface StackIsAllowedCheck {
            boolean isAllowed(ItemStack stack);
        }

        /**
         * Get the augment key this belongs to
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @NotNull
        <C extends PerItemStack, T extends ItemConvertible> AugmentKey.PerItemStack<C, T> getAugmentKey();

        /**
         * Return the proxied nbt compound.
         *
         * @since    0.1.0
         */
        @NotNull
        @Override
        default NbtCompound getProxiedNbtCompound() {
            return this.getNbt();
        }

        /**
         * Get the root NbtCompound of the stack this augment is working with.
         * This should probably be the NBT provided by the instantiator.
         *
         * @since    0.1.0
         */
        @NotNull
        NbtCompound getItemStackNbt();

        /**
         * Get the NbtCompound this augment is working with.
         * This should probably be the namespaced one.
         *
         * @since    0.1.0
         */
        @NotNull
        NbtCompound getNbt();

        /**
         * Method to get the ItemStack of this augment
         *
         * @since    0.1.0
         */
        ItemStack getItemStack();

        /**
         * Mark the element as dirty
         *
         * @since    0.1.0
         */
        default void markDirty() {
            this.setDirty(true);
        }
    }

    /**
     * An augment per block.
     * Useful when it should always be loaded, which a BlockEntity can't do.
     *
     * @since    0.2.0
     */
    interface InternalPerBlock extends Augment {

        /**
         * The functional interface used to create new instances
         *
         * @since    0.2.0
         */
        @FunctionalInterface
        interface Instantiator<C extends InternalPerBlock> {
            C create(World world, BlockPos origin);
        }

        /**
         * Return the origin position of this augment.
         *
         * @since    0.2.0
         */
        @NotNull
        BlockPos getOrigin();

        /**
         * Fetch the world associated with this augment.
         *
         * @since    0.2.0
         */
        @NotNull
        World getWorld();

        /**
         * Should this augment be kept?
         * When this returns false, it will be removed.
         *
         * @since    0.2.0
         */
        boolean shouldPersist();

        /**
         * Do something on each tick
         *
         * @since    0.2.0
         */
        default void onTick() {}
    }

    /**
     * An augment per block.
     * Useful when it should always be loaded, which a BlockEntity can't do.
     *
     * @since    0.2.0
     */
    interface PerBlock extends InternalPerBlock {

        // All the registered per-block-pos augments
        Map<AugmentKey.PerBlock<?>, Class<?>> REGISTRY = new HashMap<>();

        // All the augments that tick
        Map<AugmentKey.PerBlock<?>, Class<?>> TICKS_WITH_WORLD = new HashMap<>();

        /**
         * Register a PerBlock augment
         *
         * @since    0.2.0
         */
        static <C extends PerBlock> AugmentKey.PerBlock<C> register(Identifier id, Class<C> augment_class, boolean tick_with_world, PerBlock.Instantiator<C> instantiator) {
            AugmentKey.PerBlock<C> key = new AugmentKey.PerBlock<>(id, augment_class, tick_with_world, instantiator);
            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (tick_with_world) {
                TICKS_WITH_WORLD.put(key, augment_class);
            }

            return key;
        }

        /**
         * Mark this augment instance as dirty
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default void markDirty() {
            this.setDirty(true);

            var key = this.getAugmentKey();

            key.onDirtyInstance(this);
        }

        /**
         * Get the augment key this belongs to
         *
         * @since    0.2.0
         */
        @NotNull
        <C extends PerBlock> AugmentKey.PerBlock<C> getAugmentKey();
    }

    /**
     * An augment for blocks affecting multiple chunks in irregular shapes.
     * An Augment of this type will always be loaded when the world is loaded.
     *
     * @since    0.2.0
     */
    interface PerChunkZone extends InternalPerBlock {

        // All the registered chunk zone augments
        Map<AugmentKey.PerChunkZone<?>, Class<?>> REGISTRY = new HashMap<>();

        // All the augments that tick
        Map<AugmentKey.PerChunkZone<?>, Class<?>> TICKS_WITH_WORLD = new HashMap<>();

        /**
         * Register a chunk zone augment
         *
         * @since    0.2.0
         */
        static <C extends PerChunkZone> AugmentKey.PerChunkZone<C> register(Identifier id, Class<C> augment_class, boolean tick_with_world, Instantiator<C> instantiator) {
            AugmentKey.PerChunkZone<C> key = new AugmentKey.PerChunkZone<>(id, augment_class, tick_with_world, instantiator);
            ALL_AUGMENTS.put(key, augment_class);
            REGISTRY.put(key, augment_class);

            if (tick_with_world) {
                TICKS_WITH_WORLD.put(key, augment_class);
            }

            return key;
        }

        /**
         * Mark this augment instance as dirty
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @Override
        default void markDirty() {
            this.setDirty(true);

            var key = this.getAugmentKey();

            key.onDirtyInstance(this);
        }

        /**
         * Get the augment key this belongs to
         *
         * @since    0.2.0
         */
        @NotNull
        <C extends PerChunkZone> AugmentKey.PerChunkZone<C> getAugmentKey();

        /**
         * Return the chunks affected by this augment.
         *
         * @since    0.2.0
         */
        @NotNull
        Set<ChunkPos> getAffectedChunks();

        /**
         * Check if this augment affects a given chunk
         *
         * @since    0.2.0
         */
        default boolean affects(ChunkPos pos) {
            var set = this.getAffectedChunks();
            return set.contains(pos);
        }

        /**
         * Check if this augment affects a given position.
         *
         * @since    0.2.0
         */
        boolean affects(BlockPos pos);
    }
}