package rocks.blackblock.bib.bv.value;

import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.collection.WeakValueHashMap;
import rocks.blackblock.bib.util.BibText;

/**
 * A simple string that represents a tag,
 * but which should be shared between multiple instances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvTag extends BvString {

    private static final WeakValueHashMap<String, BvTag> INSTANCES = new WeakValueHashMap<>();

    public static final String TYPE = "tag";

    private String title = null;
    private String generated_title = null;

    /**
     * Get or create a tag
     *
     * @since    0.2.0
     */
    public static BvTag get(String name) {

        if (INSTANCES.containsKey(name)) {
            return INSTANCES.get(name);
        }

        BvTag result = new BvTag(name);
        INSTANCES.put(name, result);
        return result;
    }

    /**
     * Needed for deserialization
     * @TODO: Implement proper deserialization of unique BvElement values
     *
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static BvTag createUnsafeEmptyTag() {
        return new BvTag(null);
    }

    /**
     * Don't allow tags to be created directly
     *
     * @since    0.2.0
     */
    private BvTag(String tag) {
        super(tag);
    }

    /**
     * Get the identifier of this type
     *
     * @since    0.2.0
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Set a combined title
     *
     * @since 0.2.0
     */
    public BvTag setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the title of this value (for visual stuff)
     *
     * @since    0.2.0
     */
    @Override
    public String getDisplayTitle() {

        if (this.title != null) {
            return this.title;
        }

        if (this.generated_title != null) {
            return this.generated_title;
        }

        StringBuilder result = new StringBuilder();
        String name = this.getContainedValue();

        if (name != null) {
            this.generated_title = BibText.titleize(name);
        }

        return this.generated_title;
    }

    /**
     * Set the value of the tag
     *
     * @since    0.2.0
     */
    @Override
    public void setContainedValue(String value) {

        if (this.contained_value != null) {
            throw new IllegalStateException("Can't change the value of a tag");
        }

        this.contained_value = value;
    }
}
