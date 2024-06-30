package rocks.blackblock.bib.bv.value;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common Number class
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class
BvNumber<ContainedType extends Number, OwnType extends BvNumber<?, ?>>
    extends AbstractBvType<ContainedType, OwnType> {

    /**
     * Convert to a string for use in commands
     *
     * @since    0.1.0
     */
    @NotNull
    public String toCommandString() {

        Number value = this.getContainedValue();

        if (value == null) {
            return "null";
        }  else {
            return value.toString();
        }
    }

    /**
     * Convert to pretty text
     *
     * @since    0.1.0
     */
    @Nullable
    @Override
    public Text toPrettyText() {

        Number value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        return Text.literal(value.toString()).formatted(Formatting.YELLOW);
    }
}
