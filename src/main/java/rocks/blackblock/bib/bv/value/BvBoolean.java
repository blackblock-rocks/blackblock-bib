package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A BV Boolean
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvBoolean extends AbstractBvType<Boolean, BvBoolean> {

    public static final String TYPE = "boolean";

    /**
     * Construct a BvBoolean with a pre-defined value
     *
     * @since    0.2.0
     */
    public BvBoolean(boolean value) {
        this.setContainedValue(value);
    }

    /**
     * Construct a BvBoolean with no value
     *
     * @since    0.2.0
     */
    public BvBoolean() {}

    /**
     * Create an instance of the given value
     *
     * @since    0.2.0
     */
    public static BvBoolean of(Boolean value) {
        return new BvBoolean(value);
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
     * Load from the given NBT value
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromNbt(NbtElement nbt_value) {

        if (nbt_value == null) {
            this.setContainedValue(null);
            return;
        }

        if (nbt_value instanceof AbstractNbtNumber nbt_number) {
            Number nr = nbt_number.numberValue();

            if (nr == null || nr.intValue() == 0) {
                this.setContainedValue(false);
            } else {
                this.setContainedValue(true);
            }
        } else if (nbt_value instanceof NbtString nbt_string) {
            String val = nbt_string.asString();

            if (val == null || val.isEmpty() || val.equals("false")) {
                this.setContainedValue(false);
            } else {
                this.setContainedValue(true);
            }
        } else {
            this.setContainedValue(null);
        }
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtInt toNbt() {

        Boolean contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        if (contained_value) {
            return NbtInt.of(1);
        } else {
            return NbtInt.of(0);
        }
    }

    /**
     * Serialize this value to JSON
     *
     * @since    0.2.0
     */
    @Override
    @Nullable
    public JsonElement toJson() {

        Boolean contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        if (contained_value) {
            return new JsonPrimitive(true);
        }

        return new JsonPrimitive(false);
    }

    /**
     * Load from the given JSON value
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

        if (json == null) {
            this.setContainedValue(null);
            return;
        }

        if (!(json instanceof JsonPrimitive json_prim)) {
            throw new RuntimeException("Expected a JsonPrimitive, but got " + json.getClass().getSimpleName());
        }

        this.setContainedValue(json_prim.getAsBoolean());
    }

    /**
     * Convert to a string for use in commands
     *
     * @since    0.2.0
     */
    @NotNull
    public String toCommandString() {

        Boolean value = this.getContainedValue();

        if (value == null) {
            return "null";
        } else if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @Nullable
    @Override
    public Text toPrettyText() {

        Boolean value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        MutableText result;

        if (value == null) {
            result = Text.literal("null").formatted(Formatting.GRAY);
        } else if (value) {
            result = Text.literal("true").formatted(Formatting.GREEN);
        } else {
            result = Text.literal("false").formatted(Formatting.RED);
        }

        return result;
    }

    /**
     * Get the string to use in placeholders
     *
     * @since    0.2.0
     */
    @Override
    public String toPlaceholderString() {
        return this.contained_value ? "true" : "false";
    }
}
