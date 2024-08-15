package rocks.blackblock.bib.util;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Library class for working with Chunks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public final class BibChunk {

    /**
     * Try to immediately get the chunk at the given position
     * @since 0.2.0
     */
    @Nullable
    public static Chunk getChunkNow(WorldView world, BlockPos pos) {
        return getChunkNow(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Try to immediately get the chunk at the given position
     * @since 0.2.0
     */
    @Nullable
    public static Chunk getChunkNow(WorldView world, int chunk_x, int chunk_z) {
        if (world instanceof ServerWorld server_world) {
            return getChunkFromHolder(getChunkHolder(server_world, chunk_x, chunk_z));
        } else {
            return world.getChunk(chunk_x, chunk_z, ChunkStatus.FULL, false);
        }
    }

    /**
     * Try to immediately get the chunk out of the given ChunkHolder
     * @since 0.2.0
     */
    @Nullable
    public static WorldChunk getChunkFromHolder(ChunkHolder holder) {
        return holder != null ? getChunkFromFuture(holder.getAccessibleFuture()) : null;
    }

    /**
     * Try to immediately get the chunk out of a completable future
     * @since 0.2.0
     */
    @Nullable
    public static WorldChunk getChunkFromFuture(CompletableFuture<OptionalChunk<WorldChunk>> chunkFuture) {
        OptionalChunk<WorldChunk> chunkResult;
        if (chunkFuture == ChunkHolder.UNLOADED_WORLD_CHUNK_FUTURE || (chunkResult = chunkFuture.getNow(null)) == null) {
            return null;
        }

        return chunkResult.orElse(null);
    }

    /**
     * Get the ChunkHolder for the given chunk
     * @since 0.2.0
     */
    @Nullable
    private static ChunkHolder getChunkHolder(ServerWorld server_world, int chunk_x, int chunk_z) {
        return server_world.getChunkManager().getChunkHolder(ChunkPos.toLong(chunk_x, chunk_z));
    }

    /**
     * Is the given chunk loaded?
     * @since 0.2.0
     */
    public static boolean isChunkLoaded(ServerWorld world, ChunkPos chunk_pos) {
        return isChunkLoaded(world, chunk_pos.x, chunk_pos.z);
    }

    /**
     * Is the given chunk loaded?
     * @since 0.2.0
     */
    public static boolean isChunkLoaded(World world, BlockPos block_pos) {
        return isChunkLoaded(world, block_pos.getX() >> 4, block_pos.getZ() >> 4);
    }

    /**
     * Is the given chunk loaded?
     * @since 0.2.0
     */
    public static boolean isChunkLoaded(ServerWorld world, BlockPos block_pos) {
        return isChunkLoaded(world, block_pos.getX() >> 4, block_pos.getZ() >> 4);
    }

    /**
     * Is the given chunk loaded?
     * @since 0.2.0
     */
    public static boolean isChunkLoaded(World world, int chunk_x, int chunk_z) {
        if (world instanceof ServerWorld server_world) {
            return isChunkLoaded(getChunkHolder(server_world, chunk_x, chunk_z));
        } else {
            return true;
        }
    }

    public static boolean isChunkLoaded(ServerWorld server_world, int chunk_x, int chunk_z) {
        return isChunkLoaded(getChunkHolder(server_world, chunk_x, chunk_z));
    }

    /**
     * Is the given chunk loaded?
     * @since 0.2.0
     */
    public static boolean isChunkLoaded(ChunkHolder holder) {
        return getChunkFromHolder(holder) != null;
    }

    /**
     * Get the biome at the given position, using a rough approximation
     * @since 0.2.0
     */
    @NotNull
    public static RegistryEntry<Biome> getRoughBiome(World world, BlockPos pos) {
        Chunk chunk = getChunkNow(world, pos);

        int x = pos.getX() >> 2;
        int y = pos.getY() >> 2;
        int z = pos.getZ() >> 2;

        return chunk != null ? chunk.getBiomeForNoiseGen(x, y, z) : world.getGeneratorStoredBiome(x, y, z);
    }
}
