package rocks.blackblock.bib.collection;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A HashMap that uses WeakReferences to store its values
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public class WeakValueHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    private static class WeakValueRef<K, V> extends WeakReference<V> {
        public K key;

        private WeakValueRef(K key, V val, ReferenceQueue q) {
            super(val, q);
            this.key = key;
        }

        private static <K, V> WeakValueRef<K, V> create(K key, V val, ReferenceQueue<WeakValueRef<K, V>> q) {
            if (val == null) return null;
            else return new WeakValueRef<>(key, val, q);
        }
    }

    /* Hash table mapping WeakKeys to values */
    private Map<K, WeakReference<V>> hash;

    /* Reference queue for cleared WeakKeys */
    private ReferenceQueue<WeakValueRef<K, V>> queue = new ReferenceQueue<>();

    /* Remove all invalidated entries from the map, that is, remove all entries
       whose values have been discarded.
     */
    private void processQueue()
    {
        WeakValueRef<K, V> ref;
        while ((ref = (WeakValueRef<K, V>) queue.poll()) != null) {
            if (ref == hash.get(ref.key)) {
                // only remove if it is the *exact* same WeakValueRef
                //
                hash.remove(ref.key);
            }
        }
    }


    /* -- Constructors -- */

    /**
     * Constructs a new, empty <code>WeakHashMap</code> with the given
     * initial capacity and the given load factor.
     *
     * @param  initialCapacity  The initial capacity of the
     *                          <code>WeakHashMap</code>
     *
     * @param  loadFactor       The load factor of the <code>WeakHashMap</code>
     *
     * @throws IllegalArgumentException  If the initial capacity is less than
     *                                   zero, or if the load factor is
     *                                   nonpositive
     */
    public WeakValueHashMap(int initialCapacity, float loadFactor)
    {
        hash = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty <code>WeakHashMap</code> with the given
     * initial capacity and the default load factor, which is
     * <code>0.75</code>.
     *
     * @param  initialCapacity  The initial capacity of the
     *                          <code>WeakHashMap</code>
     *
     * @throws IllegalArgumentException  If the initial capacity is less than
     *                                   zero
     */
    public WeakValueHashMap(int initialCapacity)
    {
        hash = new HashMap<>(initialCapacity);
    }

    /**
     * Constructs a new, empty <code>WeakHashMap</code> with the default
     * initial capacity and the default load factor, which is
     * <code>0.75</code>.
     */
    public WeakValueHashMap()
    {
        hash = new HashMap<>();
    }

    /**
     * Constructs a new <code>WeakHashMap</code> with the same mappings as the
     * specified <tt>Map</tt>.  The <code>WeakHashMap</code> is created with an
     * initial capacity of twice the number of mappings in the specified map
     * or 11 (whichever is greater), and a default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param   t the map whose mappings are to be placed in this map.
     * @since    1.3
     */
    public WeakValueHashMap(Map t)
    {
        this(Math.max(2*t.size(), 11), 0.75f);
        putAll(t);
    }

    /* -- Simple queries -- */

    /**
     * Returns the number of key-value mappings in this map.
     * <strong>Note:</strong> <em>In contrast with most implementations of the
     * <code>Map</code> interface, the time required by this operation is
     * linear in the size of the map.</em>
     */
    public int size()
    {
        processQueue();
        return hash.size();
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        processQueue();
        return hash.isEmpty();
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     */
    public boolean containsKey(Object key)
    {
        processQueue();
        return hash.containsKey(key);
    }

    /* -- Lookup and modification operations -- */

    /**
     * Returns the value to which this map maps the specified <code>key</code>.
     * If this map does not contain a value for this key, then return
     * <code>null</code>.
     *
     * @param  key  The key whose associated value, if any, is to be returned
     */
    public V get(Object key)
    {
        processQueue();
        WeakReference<V> ref = hash.get(key);
        if (ref != null) return ref.get();
        return null;
    }

    /**
     * Updates this map so that the given <code>key</code> maps to the given
     * <code>value</code>.  If the map previously contained a mapping for
     * <code>key</code> then that mapping is replaced and the previous value is
     * returned.
     *
     * @param  key    The key that is to be mapped to the given
     *                <code>value</code>
     * @param  value  The value to which the given <code>key</code> is to be
     *                mapped
     *
     * @return  The previous value to which this key was mapped, or
     *          <code>null</code> if if there was no mapping for the key
     */
    public V put(K key, V value) {
        processQueue();
        WeakReference<V> rtn = hash.put(key, WeakValueRef.create(key, value, queue));

        if (rtn == null) {
            return null;
        }

        return rtn.get();
    }

    /**
     * Removes the mapping for the given <code>key</code> from this map, if
     * present.
     *
     * @param  key  The key whose mapping is to be removed
     *
     * @return  The value to which this key was mapped, or <code>null</code> if
     *          there was no mapping for the key
     */
    public V remove(Object key) {
        processQueue();
        WeakReference<V> rtn = hash.remove(key);

        if (rtn == null) {
            return null;
        }

        return rtn.get();
    }


    /**
     * Return the entryset (for iterators)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.3.1
     */
    public Set<Map.Entry<K, V>> entrySet()  {
        processQueue();

        Set<Map.Entry<K, V>> result = hash.entrySet().stream().map(reference_entry -> {

            WeakReference<V> value = reference_entry.getValue();

            if (value == null || value.get() == null) {
                return null;
            }

            Map.Entry<K, V> result_entry = new SimpleEntry<>(reference_entry.getKey(), value.get());

            return result_entry;
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        return result;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        processQueue();
        hash.clear();
    }
}
