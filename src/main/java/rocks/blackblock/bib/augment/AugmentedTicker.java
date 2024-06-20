package rocks.blackblock.bib.augment;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

/**
 * Ad-hoc things that tick with something.
 * Kind of like an augment, but it doesn't store data
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public interface AugmentedTicker {

    /**
     * Ticks with a chunk
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    interface WithChunk extends AugmentedTicker {

        /**
         * Tick the chunks for the given world
         *
         * @since    0.1.0
         */
        static void tickWorld(ServerWorld world) {

            Map<ChunkPos, Set<WithChunk>> sets_per_chunk_pos = WithChunkAttachments.PER_WORLD.get(world);

            if (sets_per_chunk_pos == null) {
                return;
            }

            sets_per_chunk_pos.forEach((chunkPos, withChunks) -> {

                if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                    return;
                }

                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

                for (WithChunk withChunk : withChunks) {
                    withChunk.tickWithChunk(world, chunk);
                }
            });
        }

        /**
         * Attach to a chunk
         *
         * @since    0.1.0
         */
        default void attachToChunk(ServerWorld world, ChunkPos chunk_pos) {
            WithChunkAttachments attachments = WithChunkAttachments.getFor(this);
            attachments.attachTo(world, chunk_pos);
        }

        /**
         * Detach from a chunk
         *
         * @since    0.1.0
         */
        default void detachFromChunk(ServerWorld world, ChunkPos chunk_pos) {
            WithChunkAttachments attachments = WithChunkAttachments.getFor(this);
            attachments.detachFrom(world, chunk_pos);
        }

        /**
         * Detach from all chunks
         *
         * @since    0.1.0
         */
        default void detachFromAllChunks() {
            WithChunkAttachments attachments = WithChunkAttachments.getFor(this);
            attachments.destroy();
        }

        /**
         * Tick with the given chunk
         *
         * @param   world   The world the chunk is in
         * @param   chunk   The chunk itself
         */
        void tickWithChunk(ServerWorld world, Chunk chunk);
    }

    /**
     * Class to keep track of where a WithChunk is attached to
     *
     * @since    0.1.0
     */
    class WithChunkAttachments {

        protected static final Map<ServerWorld, Map<ChunkPos, Set<WithChunk>>> PER_WORLD = new HashMap<>();
        protected static final Map<WithChunk, WithChunkAttachments> ATTACHMENTS = new WeakHashMap<>();

        // The WithChunk instance this belongs to
        private final WithChunk with_chunk;

        // All the chunks this is attached to
        private final Map<ServerWorld, Set<ChunkPos>> attached_chunks = new HashMap<>();

        /**
         * Initialize the new instance
         *
         * @since    0.1.0
         */
        public WithChunkAttachments(WithChunk with_chunk) {
            this.with_chunk = with_chunk;
        }

        /**
         * Get/create the `WithChunkAttachments` for the given `WithChunk` instance
         *
         * @since    0.1.0
         */
        public static WithChunkAttachments getFor(WithChunk with_chunk) {
            return ATTACHMENTS.computeIfAbsent(with_chunk, k -> new WithChunkAttachments(with_chunk));
        }

        /**
         * Attach this instance to the given world/chunk
         *
         * @since    0.1.0
         */
        public void attachTo(ServerWorld world, ChunkPos chunk_pos) {
            Map<ChunkPos, Set<WithChunk>> map = PER_WORLD.computeIfAbsent(world, k -> new HashMap<>());
            Set<WithChunk> set = map.computeIfAbsent(chunk_pos, k -> new HashSet<>());
            set.add(this.with_chunk);

            Set<ChunkPos> our_set = attached_chunks.computeIfAbsent(world, k -> new HashSet<>());
            our_set.add(chunk_pos);
        }

        /**
         * Detach this instance from the given world/chunk
         *
         * @since    0.1.0
         */
        public void detachFrom(ServerWorld world, ChunkPos chunk_pos) {
            Map<ChunkPos, Set<WithChunk>> map = PER_WORLD.get(world);

            if (map == null) {
                return;
            }

            Set<WithChunk> set = map.get(chunk_pos);

            if (set == null) {
                return;
            }

            set.remove(this.with_chunk);
        }

        /**
         * Destroy this instance.
         * Will detach from all the attached chunks
         *
         * @since    0.1.0
         */
        public void destroy() {

            attached_chunks.forEach((serverWorld, chunkPosSet) -> {

                Map<ChunkPos, Set<WithChunk>> map = PER_WORLD.get(serverWorld);

                if (map == null) {
                    return;
                }

                chunkPosSet.forEach(chunkPos -> {

                    Set<WithChunk> set = map.get(chunkPos);

                    if (set == null) {
                        return;
                    }

                    set.remove(this.with_chunk);
                });
            });

            attached_chunks.clear();
        }
    }

}
