package rocks.blackblock.bib.collection;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A cache map that keeps a value as long as it's used
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class TimeToIdleCache<K, V> {

    private final Map<K, ValueWithTimestamp<V>> map = new ConcurrentHashMap<>();
    private final long timeToIdleInMs;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> cleanupTask;

    /**
     * Initialize the cache
     */
    public TimeToIdleCache(long duration, TimeUnit unit) {
        this.timeToIdleInMs = unit.toMillis(duration);

        // We want a cleanup to happen at most once a minute
        long cleanupIntervalInMs = Math.max(this.timeToIdleInMs / 3, TimeUnit.MINUTES.toMillis(1));

        WeakReference<TimeToIdleCache<K,V>> weakThis = new WeakReference<>(this);

        this.cleanupTask = scheduler.scheduleAtFixedRate(() -> {
            TimeToIdleCache<K,V> map = weakThis.get();
            if (map == null) {
                // The map has been GC'd, cancel the cleanup task
                cleanupTask.cancel(false);
            } else {
                map.expungeExpiredValues();
            }
        }, cleanupIntervalInMs, cleanupIntervalInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Add a new value in the cache
     */
    public void put(K key, V value) {
        if (key == null) throw new NullPointerException("Key cannot be null");
        map.put(key, new ValueWithTimestamp<>(value));
    }

    /**
     * Get a value
     */
    public V get(K key) {
        if (key == null) return null;

        ValueWithTimestamp<V> valueWithTimestamp = map.get(key);
        if (valueWithTimestamp == null) {
            return null;
        }

        if (System.currentTimeMillis() - valueWithTimestamp.getTimestamp() > timeToIdleInMs) {
            map.remove(key, valueWithTimestamp);
            return null;
        }

        valueWithTimestamp.touch();
        return valueWithTimestamp.value;
    }

    /**
     * Clean up expired values
     */
    public void expungeExpiredValues() {
        long now = System.currentTimeMillis();
        map.entrySet().removeIf(entry ->
                now - entry.getValue().getTimestamp() > timeToIdleInMs);
    }

    /**
     * Wrapper for the values
     */
    private static class ValueWithTimestamp<V> {
        final V value;
        private final AtomicLong timestamp;

        ValueWithTimestamp(V value) {
            this.value = value;
            this.timestamp = new AtomicLong(System.currentTimeMillis());
        }

        void touch() {
            timestamp.set(System.currentTimeMillis());
        }

        long getTimestamp() {
            return timestamp.get();
        }
    }
}
