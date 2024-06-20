package rocks.blackblock.bib.collection;

/**
 * Allows us to compare instances differently based on the scenario.
 * For example: two ItemStacks can be considered different (for merging)
 * but "similar" for other reasons
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public interface CompareForScenario<T> {

    /**
     * Does this object have special comparison rules for the given scenario?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    boolean supportsScenario(String scenario);

    /**
     * Compare this object to another object for the given scenario.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    Boolean compareForScenario(T left, T right, String scenario);
}
