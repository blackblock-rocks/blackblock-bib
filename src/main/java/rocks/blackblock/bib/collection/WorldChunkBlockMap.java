package rocks.blackblock.bib.collection;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map World, Chunk & Block data to a value
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings({
        // Ignore unused warnings: this is a library after all
        "unused",

        // Ignore warnings of using raw types
        "rawtypes",

        // Ignore unchecked typecast warnings
        "unchecked"
})
public class WorldChunkBlockMap<V> implements Iterable<V>  {

    protected final Map<World, ChunkMap<Map<BlockPos, V>>> worldMap;
    protected Integer initialCapacity = null;

    /**
     * Create a new WorldChunkBlockMap with default settings
     *
     * @since    0.2.0
     */
    public WorldChunkBlockMap() {
        this.worldMap = new HashMap<>(6);
    }

    /**
     * Create a new WorldChunkBlockMap with the given backing for World values
     *
     * @since    0.2.0
     */
    public WorldChunkBlockMap(Map worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Create a new WorldChunkMap and use the given capacity
     * for the inner ChunkMap
     *
     * @since    0.2.0
     */
    public WorldChunkBlockMap(int initialCapacity) {
        this();
        this.initialCapacity = initialCapacity;
    }

    /**
     * Create a new ChunkMap
     *
     * @since    0.2.0
     */
    protected ChunkMap<Map<BlockPos, V>> createChunkMap() {

        if (this.initialCapacity != null) {
            return new ChunkMap<>(this.initialCapacity);
        }

        return new ChunkMap<>();
    }

    /**
     * Get the total amount of values in this map
     *
     * @since    0.2.0
     */
    public int size() {

        int result = 0;
        for (var chunkMap : this.worldMap.values()) {
            for (var blockMap : chunkMap.values()) {
                result += blockMap.size();
            }
        }

        return result;
    }

    /**
     * Is this map empty?
     *
     * @since    0.2.0
     */
    public boolean isEmpty() {

        if (this.worldMap.isEmpty()) {
            return true;
        }

        for (var chunkMap : this.worldMap.values()) {
            for (var blockMap : chunkMap.values()) {
                if (!blockMap.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Do we have values for the given world & chunk?
     *
     * @since    0.2.0
     */
    public boolean containsKey(World world, Chunk chunk) {
        return this.containsKey(world, chunk.getPos());
    }

    /**
     * Do we have any values for the given world & chunk?
     *
     * @since    0.2.0
     */
    public boolean containsKey(World world, ChunkPos chunk_pos) {

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return false;
        }

        var blockMap = chunkMap.get(chunk_pos);

        if (blockMap == null) {
            return false;
        }

        return !blockMap.isEmpty();
    }

    /**
     * Do we have the given value anywhere?
     *
     * @since    0.2.0
     */
    public boolean containsValue(Object o) {

        for (var chunkMap : this.worldMap.values()) {
            for (var blockMap : chunkMap.values()) {
                if (blockMap.containsValue(o)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the values of the given world & chunk
     *
     * @since    0.2.0
     */
    @Nullable
    public ImmutableMap<BlockPos, V> get(World world, Chunk chunk) {

        if (world == null || chunk == null) {
            return null;
        }

        return this.get(world, chunk.getPos());
    }

    /**
     * Get the values of the given world & chunk pos
     *
     * @since    0.2.0
     */
    @Nullable
    public ImmutableMap<BlockPos, V> get(World world, ChunkPos chunk_pos) {

        if (world == null || chunk_pos == null) {
            return null;
        }

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return null;
        }

        var blockMap = chunkMap.get(chunk_pos);

        if (blockMap == null || blockMap.isEmpty()) {
            return null;
        }

        return ImmutableMap.copyOf(blockMap);
    }

    /**
     * Get the value of the given world, chunk pos & block pos
     *
     * @since    0.2.0
     */
    @Nullable
    public V get(World world, ChunkPos chunk_pos, BlockPos pos) {

        if (world == null || pos == null) {
            return null;
        }

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return null;
        }

        if (chunk_pos == null) {
            chunk_pos = new ChunkPos(pos);
        }

        var blockMap = chunkMap.get(chunk_pos);

        if (blockMap == null || blockMap.isEmpty()) {
            return null;
        }

        return blockMap.get(pos);
    }

    /**
     * Get the value of the given world, chunk pos & block pos
     *
     * @since    0.2.0
     */
    @Nullable
    public V get(World world, BlockPos pos) {

        if (world == null || pos == null) {
            return null;
        }

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return null;
        }

        var chunk_pos = new ChunkPos(pos);

        var blockMap = chunkMap.get(chunk_pos);

        if (blockMap == null || blockMap.isEmpty()) {
            return null;
        }

        return blockMap.get(pos);
    }

    /**
     * Put the given world & blockpos value
     *
     * @since    0.2.0
     */
    @Nullable
    public V put(World world, BlockPos pos, V value) {

        if (world == null || pos == null) {
            return null;
        }

        return this.put(world, new ChunkPos(pos), pos, value);
    }

    /**
     * Put the given world & chunk value
     *
     * @since    0.2.0
     */
    @Nullable
    public V put(World world, ChunkPos chunk_pos, BlockPos pos, V value) {

        if (world == null || pos == null) {
            return null;
        }

        if (chunk_pos == null) {
            chunk_pos = new ChunkPos(pos);
        }

        var chunkMap = this.worldMap.computeIfAbsent(world, world1 -> this.createChunkMap());
        var blockMap = chunkMap.computeIfAbsent(chunk_pos, chunkPos -> new HashMap<>());

        return blockMap.put(pos, value);
    }

    /**
     * Remove the given value
     *
     * @since    0.2.0
     */
    public V remove(Object o) {
        V result;

        for (var chunkMap : this.worldMap.values()) {
            for (var blockMap : chunkMap.values()) {
                result = blockMap.remove(o);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Clear all the values
     *
     * @since    0.2.0
     */
    public void clear() {
        this.worldMap.clear();
    }

    /**
     * Iterate over all the values
     *
     * @since    0.2.0
     */
    @NotNull
    @Override
    public Iterator<V> iterator() {
        return new WorldChunkBlockIterator();
    }

    /**
     * Iterate over each entry with world, chunk & block pos info
     *
     * @since    0.2.0
     */
    public void forEach(WorldChunkBlockValueRunner<V> iterator) {
        this.worldMap.forEach((world, vChunkMap) -> {
            vChunkMap.forEach((chunkPos, blockMap) -> {
                blockMap.forEach((blockPos, v) -> {
                    iterator.run(world, chunkPos, blockPos, v);
                });
            });
        });
    }

    /**
     * Iterate over each entry with world & chunk pos info
     *
     * @since    0.2.0
     */
    public void forEachChunk(WorldChunkMap.TripleIterator<Map<BlockPos, V>> iterator) {
        this.worldMap.forEach((world, vChunkMap) -> {
            vChunkMap.forEach((chunkPos, blockMap) -> {
                iterator.iterate(world, chunkPos, ImmutableMap.copyOf(blockMap));
            });
        });
    }

    /**
     * Get an iterator for the given world
     *
     * @since    0.2.0
     */
    public Collection<V> valuesPerWorld(World world) {
        var chunks = this.worldMap.get(world);

        if (chunks == null) {
            return null;
        }

        List<V> result = new ArrayList<>(this.size());

        for (var chunkMap : this.worldMap.values()) {
            for (var blockMap : chunkMap.values()) {
                result.addAll(blockMap.values());
            }
        }

        return result;
    }

    /**
     * Iterate over each entry in the given world, chunk & block info
     *
     * @since    0.2.0
     */
    public void forEach(World world, WorldChunkBlockValueRunner<V> iterator) {

        var chunks = this.worldMap.get(world);

        if (chunks == null) {
            return;
        }

        chunks.forEach((chunkPos, blockMap) -> {
            blockMap.forEach((blockPos, v) -> {
                iterator.run(world, chunkPos, blockPos, v);
            });
        });
    }

    @FunctionalInterface
    public interface WorldChunkBlockValueRunner<V> {
        void run(World world, ChunkPos chunk_pos, BlockPos block_pos, V value);
    }

    /**
     * Our own iterator class
     *
     * @since    0.2.0
     */
    private class WorldChunkBlockIterator implements Iterator<V> {
        private final Iterator<Map.Entry<World, ChunkMap<Map<BlockPos, V>>>> worldIterator;
        private Iterator<Map.Entry<ChunkPos, Map<BlockPos, V>>> chunkIterator;
        private Iterator<Map.Entry<BlockPos, V>> blockIterator;

        public WorldChunkBlockIterator() {
            this.worldIterator = worldMap.entrySet().iterator();
            this.chunkIterator = null;
            this.blockIterator = null;
        }

        @Override
        public boolean hasNext() {

            // If there is a block iterator, and it has a next value,
            // we're good to go
            if (this.blockIterator != null && this.blockIterator.hasNext()) {
                return true;
            }

            if (chunkIterator != null) {
                while (chunkIterator.hasNext()) {
                    var chunkEntry = chunkIterator.next();
                    this.blockIterator = chunkEntry.getValue().entrySet().iterator();
                    if (this.blockIterator.hasNext()) {
                        return true;
                    }
                }
            }

            // Current chunk iterator is finished, get the next one
            while (worldIterator.hasNext()) {
                var worldEntry = worldIterator.next();
                this.chunkIterator = worldEntry.getValue().entrySet().iterator();
                if (chunkIterator.hasNext()) {
                    var chunkEntry = chunkIterator.next();
                    this.blockIterator = chunkEntry.getValue().entrySet().iterator();
                    if (this.blockIterator.hasNext()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return this.blockIterator.next().getValue();
        }
    }
}
