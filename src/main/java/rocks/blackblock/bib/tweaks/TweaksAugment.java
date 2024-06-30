package rocks.blackblock.bib.tweaks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.augment.Augment;
import rocks.blackblock.bib.bv.value.BvMap;

/**
 * This Augment class is how a TweaksConfiguration can be registered.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public abstract class TweaksAugment<T extends RootTweakMap> implements Augment {

    // Does this instance need saving?
    protected boolean is_dirty = false;

    // The root TweaksConfiguration we're working with
    // (Shared with all other instances)
    protected final TweaksConfiguration tweaks_configuration;

    // The actual context
    protected T data_context;

    /**
     * Initialize the instance
     *
     * @since    0.1.0
     */
    public TweaksAugment(@NotNull TweaksConfiguration tweaks_configuration, @NotNull T data_context) {
        this.tweaks_configuration = tweaks_configuration;
        this.setDataContext(data_context);
    }

    /**
     * Set the data context
     *
     * @since    0.1.0
     */
    protected void setDataContext(T root_map) {
        this.data_context = root_map;

        if (root_map != null) {
            this.data_context.setOnChangeListener(this::markDirty);
        }
    }

    /**
     * Set the dirtiness
     *
     * @since    0.1.0
     */
    @Override
    public void setDirty(boolean dirty) {
        this.is_dirty = dirty;
    }

    /**
     * Is this augment in need of saving?
     *
     * @since    0.1.0
     */
    @Override
    public boolean isDirty() {
        return this.is_dirty;
    }

    /**
     * Get the correct context for the command
     *
     * @since    0.1.0
     */
    public BvMap getDataContext() {
        return this.data_context;
    }

    /**
     * Trigger a change event for all values
     *
     * @since    0.1.0
     */
    protected void triggerChange() {
        BvMap root = this.getDataContext();
        this.tweaks_configuration.triggerChangeEvent(null, root, root);
    }

    /**
     * Read the tweaks from NBT
     *
     * @since    0.1.0
     */
    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        if (nbt != null && nbt.contains("data")) {
            this.data_context.loadFromNbt(nbt.get("data"));
        } else {
            this.data_context.clear();
        }

        this.triggerChange();
    }

    /**
     * Write the tweaks to NBT
     *
     * @since    0.1.0
     */
    @Override
    public NbtCompound writeToNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        // Call the toNbt method directly,
        // it should always be a BvMap anyway!
        NbtElement serialized = this.data_context.toNbt();

        if (serialized != null) {
            nbt.put("data", serialized);
        }

        return nbt;
    }

    /**
     * The Global TweaksAugment
     *
     * @since    0.1.0
     */
    public static class Global extends TweaksAugment<RootTweakMap> implements Augment.Global {

        /**
         * Initialize the instance
         *
         * @since 0.1.0
         */
        public Global(@NotNull TweaksConfiguration tweaks_configuration) {
            super(tweaks_configuration, new RootTweakMap());
        }
    }

    /**
     * The PerWorld TweaksAugment
     *
     * @since    0.1.0
     */
    public static class PerWorld extends TweaksAugment<RootTweakMap.ForWorld> implements Augment.PerWorld {

        private final World world;

        /**
         * Initialize the instance
         *
         * @since 0.1.0
         */
        public PerWorld(@NotNull TweaksConfiguration tweaks_configuration, World world) {
            super(tweaks_configuration, new RootTweakMap.ForWorld(world));
            this.world = world;
        }

        @Override
        public World getWorld() {
            return this.world;
        }
    }

    /**
     * The PerChunk TweaksAugment
     *
     * @since    0.1.0
     */
    public static class PerChunk extends TweaksAugment<RootTweakMap.ForChunk> implements Augment.PerChunk {

        private Chunk chunk;
        private final ServerWorld world;

        /**
         * Initialize the instance
         *
         * @since 0.1.0
         */
        public PerChunk(@NotNull TweaksConfiguration tweaks_configuration, ServerWorld world, Chunk chunk) {
            super(tweaks_configuration, new RootTweakMap.ForChunk(world, chunk));
            this.world = world;
            this.setChunk(chunk);
        }

        @Override
        public void setChunk(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public Chunk getChunk() {
            return this.chunk;
        }
    }

    /**
     * The PerPlayer TweaksAugment
     *
     * @since    0.1.0
     */
    public static class PerPlayer extends TweaksAugment<RootTweakMap.ForPlayer> implements Augment.PerPlayer {

        private ServerPlayerEntity player;

        /**
         * Initialize the instance
         *
         * @since 0.1.0
         */
        public PerPlayer(@NotNull TweaksConfiguration tweaks_configuration, ServerPlayerEntity player) {
            super(tweaks_configuration, new RootTweakMap.ForPlayer(player));
            this.player = player;
        }

        @Override
        public ServerPlayerEntity getPlayer() {
            return this.player;
        }
    }
}
