package rocks.blackblock.bib.bv.value;

import com.mojang.brigadier.Message;
import org.jetbrains.annotations.Nullable;

/**
 * A BV Value that is mostly used for a display option
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class BvDisplayOption extends BvString {

    public static final String TYPE = "display_option";

    private Message tooltip = null;

    /**
     * Create an instance of the given value
     *
     * @since    0.1.0
     */
    public static BvDisplayOption of(String slug, Message tooltip) {
        var result = new BvDisplayOption();
        result.setContainedValue(slug);
        result.tooltip = tooltip;
        return result;
    }

    /**
     * Get the identifier of this type
     *
     * @since    0.1.0
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Convert to a tooltip for use in commands
     *
     * @since    0.1.0
     */
    @Override
    @Nullable
    public Message toCommandTooltip() {
        return this.tooltip;
    }
}
