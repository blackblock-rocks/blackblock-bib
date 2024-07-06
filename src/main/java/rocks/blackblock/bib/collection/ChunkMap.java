package rocks.blackblock.bib.collection;

import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map Chunk positions to a value.
 * This does not take worlds into account,
 * use {@link WorldChunkMap} for that.
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
public class ChunkMap<V> extends HashMap<ChunkPos, V> {

    /**
     * Create a new ChunkMap without a specific capacity
     *
     * @since    0.2.0
     */
    public ChunkMap() {
        super();
    }

    /**
     * Create a new ChunkMap with the given capacity
     *
     * @since    0.2.0
     */
    public ChunkMap(int initialCapacity) {
        super(initialCapacity);
    }

}
