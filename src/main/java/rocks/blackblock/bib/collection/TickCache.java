package rocks.blackblock.bib.collection;

import rocks.blackblock.bib.util.BibServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cache map that keeps a value for a certain amount of server ticks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class TickCache<K, V> {

    private final Map<K, ValueInfo> map;
    private final int max_age;

    /**
     * Initialize a new cache instance with a regular HashMap
     */
    public TickCache(int max_age) {
        this(new HashMap<>(), max_age);
    }

    /**
     * Initialize a new cache instance with the given map as the backing
     */
    protected TickCache(Map<K, ValueInfo> map, int max_age) {
        this.map = map;
        this.max_age = max_age;
    }

    /**
     * Add a value
     */
    public void put(K key, V value) {

        this.expungeExpiredValues();

        ValueInfo info = new ValueInfo(value, BibServer.getTick());
        this.map.put(key, info);
    }

    /**
     * Get a value by its key.
     * Returns null if the value is expired or does not exist
     */
    public V get(K key) {

        this.expungeExpiredValues();

        V result = null;
        ValueInfo info = this.map.get(key);

        if (info != null) {
            result = info.getValueIfAllowed();

            if (result == null) {
                this.map.remove(key);
            }
        }

        return result;
    }

    /**
     * Remove expired values
     */
    public void expungeExpiredValues() {

        int current_tick = BibServer.getTick();
        List<K> keys_to_remove = new ArrayList<>();

        for (Map.Entry<K, ValueInfo> entry : this.map.entrySet()) {
            ValueInfo info = entry.getValue();
            if (current_tick > info.tick_death) {
                keys_to_remove.add(entry.getKey());
            }
        }

        for (K key : keys_to_remove) {
            this.map.remove(key);
        }
    }

    /**
     * Wrapper class for the actual value
     */
    protected class ValueInfo {
        private final V value;
        private final int tick_death;

        public ValueInfo(V value, int tick_birth) {
            this.value = value;
            this.tick_death = tick_birth + max_age;
        }

        public V getValueIfAllowed() {
            if (BibServer.getTick() > this.tick_death) {
                return null;
            }

            return this.value;
        }
    }
}
