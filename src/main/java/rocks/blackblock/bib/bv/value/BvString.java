package rocks.blackblock.bib.bv.value;

import carpet.script.value.StringValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.bv.operator.BvOperator;

/**
 * A BV String
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvString extends AbstractBvType<String, BvString> {

    public static BvOperator<BvString> EQUALS                = new BvOperator<>(BvString.class, "equals",   BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);
    public static BvOperator<BvString> GREATER_THAN          = new BvOperator<>(BvString.class, "gt",       BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);
    public static BvOperator<BvString> LESS_THAN             = new BvOperator<>(BvString.class, "lt",       BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);
    public static BvOperator<BvString> GREATER_THAN_OR_EQUAL = new BvOperator<>(BvString.class, "gte",      BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);
    public static BvOperator<BvString> LESS_THAN_OR_EQUAL    = new BvOperator<>(BvString.class, "lte",      BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);
    public static BvOperator<BvString> CONTAINS              = new BvOperator<>(BvString.class, "contains", BvOperator.Type.LOGICAL, BvOperator.Arity.BINARY);

    public static final String TYPE = "string";

    /**
     * Construct a BvBoolean with a pre-defined value
     *
     * @since    0.2.0
     */
    public BvString(String value) {
        this.setContainedValue(value);
    }

    /**
     * Construct a BvString with no value
     *
     * @since    0.2.0
     */
    public BvString() {}

    /**
     * Create an instance of the given value
     *
     * @since    0.2.0
     */
    public static BvString of(String value) {
        return new BvString(value);
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

        this.setContainedValue(nbt_value.asString());
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtString toNbt() {

        String contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        return NbtString.of(contained_value);
    }

    /**
     * Convert to a JSON value
     *
     * @since    0.2.0
     */
    @Override
    public JsonElement toJson() {

        String value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        return new JsonPrimitive(value);
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

        this.setContainedValue(json_prim.getAsString());
    }

    /**
     * Convert to a string for use in commands
     *
     * @since    0.2.0
     */
    @NotNull
    public String toCommandString() {

        String value = this.getContainedValue();

        if (value == null || value.isBlank()) {
            return "-";
        }  else {
            return value;
        }
    }

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @NotNull
    @Override
    public Text toPrettyText() {

        String value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        return Text.literal('"' + value + '"').formatted(Formatting.AQUA);
    }

    /**
     * Get the string to use in placeholders
     *
     * @since    0.2.0
     */
    @Override
    public String toPlaceholderString() {
        return this.contained_value;
    }

    /**
     * Execute an operator that does not have an executor
     *
     * @since    0.2.0
     */
    @Override
    public Boolean executeCustomBinaryOperator(BvOperator operator, BvElement right) {

        String other_string = null;

        if (right instanceof BvString string_value) {
            other_string = string_value.getContainedValue();
        }

        String safe_string = other_string;

        if (safe_string == null) {
            safe_string = "";
        }

        if (operator == EQUALS) {
            return this.contained_value.equals(other_string);
        }

        if (operator == GREATER_THAN) {
            return this.contained_value.compareTo(safe_string) > 0;
        }

        if (operator == LESS_THAN) {
            return this.contained_value.compareTo(safe_string) < 0;
        }

        if (operator == GREATER_THAN_OR_EQUAL) {
            return this.contained_value.compareTo(safe_string) >= 0;
        }

        if (operator == LESS_THAN_OR_EQUAL) {
            return this.contained_value.compareTo(safe_string) <= 0;
        }

        if (operator == CONTAINS) {
            return this.contained_value.contains(safe_string);
        }

        return null;
    }
}
