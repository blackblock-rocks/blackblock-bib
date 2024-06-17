package rocks.blackblock.bib.collection;

import rocks.blackblock.bib.util.BibServer;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache map that keeps a value for a certain amount of server ticks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class TickCache<K, V> {

    private Map<K, ValueInfo> map;
    private int max_age;

    public TickCache(int max_age) {
        this(new HashMap<>(), max_age);
    }

    protected TickCache(Map<K, ValueInfo> map, int max_age) {
        this.map = map;
        this.max_age = max_age;
    }

    public void put(K key, V value) {
        ValueInfo info = new ValueInfo(value, BibServer.getTick());
        this.map.put(key, info);
    }

    public V get(K key) {

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

    protected class ValueInfo {
        private V value;
        private int tick_birth;
        private int tick_death;

        public ValueInfo(V value, int tick_birth) {
            this.value = value;
            this.tick_birth = tick_birth;
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
