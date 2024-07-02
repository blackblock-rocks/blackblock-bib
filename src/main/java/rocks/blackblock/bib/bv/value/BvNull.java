package rocks.blackblock.bib.bv.value;


import com.google.gson.JsonElement;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * A value representing null
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public class BvNull extends AbstractBvType<BvNull, BvNull> {

    public static BvNull NULL = new BvNull();
    public static final String TYPE = "null";

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
     * Load from JSON
     * (Which does nothing, because this value is null)
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

    }

    /**
     * Convert this to a Json element
     *
     * @since    0.2.0
     */
    @Override
    public JsonElement toJson() {
        return null;
    }

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @Nullable
    @Override
    public Text toPrettyText() {
       return Text.literal("null").formatted(Formatting.GRAY);
    }

    /**
     * Load from the given NBT value
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromNbt(NbtElement nbt_value) {
        
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtString toNbt() {
        return null;
    }

    /**
     * Get the contained value
     *
     * @since    0.2.0
     */
    @Override
    public BvNull getContainedValue() {
        return NULL;
    }

    /**
     * Set the actual value
     * (Which does nothing, because this value is null)
     *
     * @since    0.2.0
     */
    @Override
    public void setContainedValue(BvNull value) {}

    /**
     * Get a string value to use in a placeholder
     *
     * @since    0.2.0
     */
    @Override
    public String toPlaceholderString() {
        return "null";
    }

    /**
     * Return a string representation of this object.
     *
     * @since    0.2.0
     */
    @Override
    public String toString() {
        return "NullValue{}";
    }

    /**
     * Check if this matches the other FlowValue
     * (Which is always true for Null values)
     *
     * @since    0.2.0
     */
    @Override
    public boolean equalsOtherValue(BvNull object) {
        return true;
    }
}
