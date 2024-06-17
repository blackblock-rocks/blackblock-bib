package rocks.blackblock.bib.collection;

import java.util.WeakHashMap;

/**
 * A cache map that keeps a value weakly for a certain amount of server ticks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class WeakTickCache<K, V> extends TickCache<K, V> {

    public WeakTickCache(int max_age) {
        super(new WeakHashMap<>(), max_age);
    }
}
