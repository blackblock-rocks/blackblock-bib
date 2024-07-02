package rocks.blackblock.bib.tweaks;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import rocks.blackblock.bib.bv.value.BvMap;

/**
 * A map to use as the root for Tweak settings
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class RootTweakMap extends BvMap {

    protected Runnable on_change_listener = null;

    /**
     * Set the on-change listener
     *
     * @since    0.2.0
     */
    public void setOnChangeListener(Runnable listener) {
        this.on_change_listener = listener;
    }

    /**
     * Fire the change listener
     *
     * @since    0.2.0
     */
    public void fireOnChangeListener() {
        if (this.on_change_listener != null) {
            this.on_change_listener.run();
        }
    }

    /**
     * Root tweak map with player info
     *
     * @since    0.2.0
     */
    public static class ForPlayer extends RootTweakMap {

        private final ServerPlayerEntity player;

        /**
         * Initialize the class instance
         *
         * @since    0.2.0
         */
        public ForPlayer(ServerPlayerEntity player) {
            this.player = player;
        }

        /**
         * Get the player
         *
         * @since    0.2.0
         */
        public ServerPlayerEntity getPlayer() {
            return this.player;
        }
    }

    /**
     * Root tweak map with world info
     *
     * @since    0.2.0
     */
    public static class ForWorld extends RootTweakMap {

        private final World world;

        /**
         * Initialize the class instance
         *
         * @since    0.2.0
         */
        public ForWorld(World world) {
            this.world = world;
        }

        /**
         * Get the world
         *
         * @since    0.2.0
         */
        public World getWorld() {
            return this.world;
        }
    }

    /**
     * Root tweak map with chunk info
     *
     * @since    0.2.0
     */
    public static class ForChunk extends RootTweakMap.ForWorld {

        private final Chunk chunk;

        /**
         * Initialize the class instance
         *
         * @since    0.2.0
         */
        public ForChunk(ServerWorld world, Chunk chunk) {
            super(world);
            this.chunk = chunk;
        }

        /**
         * Get the world
         *
         * @since    0.2.0
         */
        public Chunk getChunk() {
            return this.chunk;
        }
    }
}
