package rocks.blackblock.bib.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Library class for working with random numbers & chances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibRandom {

    public static final Random RANDOM = new Random();

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
