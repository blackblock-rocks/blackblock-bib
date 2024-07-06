package rocks.blackblock.bib.collection;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map World & Chunk data to a value
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
public class WorldChunkMap<V> implements Iterable<V> {

    private final Map<World, ChunkMap<V>> worldMap;
    private Integer initialCapacity = null;

    /**
     * Create a new WorldChunkMap with default settings
     *
     * @since    0.2.0
     */
    public WorldChunkMap() {
        this.worldMap = new HashMap<>(6);
    }

    /**
     * Create a new WorldChunkMap with the given backing for World values
     *
     * @since    0.2.0
     */
    public WorldChunkMap(Map worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Create a new WorldChunkMap and use the given capacity
     * for the inner ChunkMap
     *
     * @since    0.2.0
     */
    public WorldChunkMap(int initialCapacity) {
        this();
        this.initialCapacity = initialCapacity;
    }

    /**
     * Create a new ChunkMap
     *
     * @since    0.2.0
     */
    protected ChunkMap<V> createChunkMap() {

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
            result += chunkMap.size();
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
            if (!chunkMap.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Do we have a value for the given world & chunk?
     *
     * @since    0.2.0
     */
    public boolean containsKey(World world, Chunk chunk) {
        return this.containsKey(world, chunk.getPos());
    }

    /**
     * Do we have a value for the given world & chunk?
     *
     * @since    0.2.0
     */
    public boolean containsKey(World world, ChunkPos chunk_pos) {

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return false;
        }

        return chunkMap.containsKey(chunk_pos);
    }

    /**
     * Do we have the given value anywhere?
     *
     * @since    0.2.0
     */
    public boolean containsValue(Object o) {

        for (var chunkMap : this.worldMap.values()) {
            if (chunkMap.containsValue(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the value of the given world & chunk
     *
     * @since    0.2.0
     */
    public V get(World world, Chunk chunk) {
        return this.get(world, chunk.getPos());
    }

    /**
     * Get the value of the given world & chunk
     *
     * @since    0.2.0
     */
    public V get(World world, ChunkPos chunk_pos) {

        var chunkMap = this.worldMap.get(world);

        if (chunkMap == null) {
            return null;
        }

        return chunkMap.get(chunk_pos);
    }

    /**
     * Put the given world & chunk value
     *
     * @since    0.2.0
     */
    @Nullable
    public V put(World world, Chunk chunk, V value) {
        return this.put(world, chunk.getPos(), value);
    }

    /**
     * Put the given world & chunk value
     *
     * @since    0.2.0
     */
    @Nullable
    public V put(World world, ChunkPos chunk_pos, V value) {
        var chunkMap = this.worldMap.computeIfAbsent(world, world1 -> this.createChunkMap());
        return chunkMap.put(chunk_pos, value);
    }

    /**
     * Remove the given value
     *
     * @since    0.2.0
     */
    public V remove(Object o) {
        V result;

        for (var chunkMap : this.worldMap.values()) {
            result = chunkMap.remove(o);

            if (result != null) {
                return result;
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
        return new WorldChunkIterator();
    }

    /**
     * Iterate over each entry with world & chunk info
     *
     * @since    0.2.0
     */
    public void forEach(TripleIterator<V> iterator) {
        this.worldMap.forEach((world, vChunkMap) -> {
            vChunkMap.forEach((chunkPos, v) -> {
                iterator.iterate(world, chunkPos, v);
            });
        });
    }

    /**
     * Iterate over each entry in the given world & chunk info
     *
     * @since    0.2.0
     */
    public void forEach(World world, TripleIterator<V> iterator) {

        var chunks = this.worldMap.get(world);

        if (chunks == null) {
            return;
        }

        chunks.forEach((chunkPos, v) -> {
            iterator.iterate(world, chunkPos, v);
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

        return chunks.values();
    }

    @FunctionalInterface
    public interface TripleIterator<V> {
        void iterate(World world, ChunkPos chunk_pos, V value);
    }

    /**
     * Our own iterator class
     *
     * @since    0.2.0
     */
    private class WorldChunkIterator implements Iterator<V> {
        private final Iterator<Map.Entry<World, ChunkMap<V>>> worldIterator;
        private Iterator<Map.Entry<ChunkPos, V>> chunkIterator;

        public WorldChunkIterator() {
            this.worldIterator = worldMap.entrySet().iterator();
            this.chunkIterator = null;
        }

        @Override
        public boolean hasNext() {

            // If we have a chunk iterator, and it has a next value,
            // we're good to go
            if (chunkIterator != null && chunkIterator.hasNext()) {
                return true;
            }

            // Current chunk iterator is finished, get the next one
            while (worldIterator.hasNext()) {
                Map.Entry<World, ChunkMap<V>> worldEntry = worldIterator.next();
                chunkIterator = worldEntry.getValue().entrySet().iterator();
                if (chunkIterator.hasNext()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return chunkIterator.next().getValue();
        }
    }
}
