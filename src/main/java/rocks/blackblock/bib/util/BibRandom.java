package rocks.blackblock.bib.util;

import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.interfaces.HasWeight;
import rocks.blackblock.bib.random.ConcurrentRandom;

import java.util.*;

/**
 * Library class for working with random numbers & chances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibRandom {

    public static final ConcurrentRandom RANDOM = new ConcurrentRandom();

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibRandom() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Use the randomizer to return true or false,
     * based on the percentage chance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasChance(int percentage) {
        int result = RANDOM.nextInt(100);
        return result <= percentage;
    }

    /**
     * Do a 50% chance test
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasChance() {
        return hasChance(50);
    }

    /**
     * Get a random entry from a list
     *
     * @author  Jelle De Loecker <jelle@elevenways.be>
     * @since   0.1.0
     */
    @Nullable
    public static <T> T getRandomEntry(List<T> list) {

        int size = list.size();

        if (size == 0) {
            return null;
        }

        if (size == 1) {
            return list.get(0);
        }

        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * Get a random entry from a collection
     *
     * @author  Jelle De Loecker <jelle@elevenways.be>
     * @since   0.1.0
     */
    @Nullable
    public static <T> T getRandomEntry(Collection<T> list) {
        return getRandomEntry(list.stream().toList());
    }

    /**
     * Take a random entry from a collection,
     * and remove it from the collection
     *
     * @author  Jelle De Loecker <jelle@elevenways.be>
     * @since   0.2.0
     */
    @Nullable
    public static <T> T takeRandomEntry(Collection<T> list) {

        var value = BibRandom.getRandomEntry(list);

        if (value == null) {
            return null;
        }

        list.remove(value);

        return value;
    }

    /**
     * Get a random entry from a map
     *
     * @author  Jelle De Loecker <jelle@elevenways.be>
     * @since   0.1.0
     */
    @Nullable
    public static <K, V> Map.Entry<K, V> getRandomEntry(Map<K, V> map) {
        return getRandomEntry(map.entrySet().stream().toList());
    }

    /**
     * Take a random entry from a map,
     * and remove it from the map
     *
     * @author  Jelle De Loecker <jelle@elevenways.be>
     * @since   0.2.0
     */
    @Nullable
    public static <K, V> Map.Entry<K, V> takeRandomEntry(Map<K, V> map) {

        var keys = map.keySet();
        var random_key = BibRandom.getRandomEntry(keys);

        if (random_key == null) {
            return null;
        }

        var value = map.remove(random_key);

        return new AbstractMap.SimpleEntry<>(random_key, value);
    }

    /**
     * Get a random entry from a weighted list
     * @since   0.2.0
     */
    public static <T extends HasWeight> T getRandomEntryFromWeightedCollection(Collection<T> list, Random random) {

        int total_weight = 0;

        for (T entry : list) {
            total_weight += entry.getWeight();
        }

        int random_value = random.nextInt(total_weight);
        int current_sum = 0;

        for (T entry : list) {
            current_sum += entry.getWeight();
            if (random_value < current_sum) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Get the next integer
     *
     * @since   0.2.0
     */
    public static int nextInt() {
        return RANDOM.nextInt();
    }

    /**
     * Get the next integer
     *
     * @since   0.2.0
     */
    public static int nextInt(Number origin, Number bound) {

        int start = 0;
        int end = 10;

        if (origin != null) {
            start = origin.intValue();
        }

        if (bound != null) {
            end = bound.intValue();
        }

        if (start == end) {
            start -= 5;
        }

        return RANDOM.nextInt(start, end);
    }

    /**
     * Get the next double
     *
     * @since   0.2.0
     */
    public static double nextDouble() {
        return RANDOM.nextDouble();
    }

    /**
     * Get the next double
     *
     * @since   0.2.0
     */
    public static double nextDouble(Number origin, Number bound) {

        double start = 0;
        double end = 10;

        if (origin != null) {
            start = origin.doubleValue();
        }

        if (bound != null) {
            end = bound.doubleValue();
        }

        if (start == end) {
            start -= 5;
        }

        return RANDOM.nextDouble(start, end);
    }
}
