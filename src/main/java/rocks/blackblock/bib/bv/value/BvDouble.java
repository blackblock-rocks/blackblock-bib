package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

/**
 * A BV Double
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvDouble extends BvNumber<Double, BvDouble> {

    public static final String TYPE = "double";

    /**
     * Construct a double with a pre-defined value
     *
     * @since    0.2.0
     */
    public BvDouble(Double value) {
        this.setContainedValue(value);
    }

    /**
     * Construct a BvDouble with no value
     *
     * @since    0.2.0
     */
    public BvDouble() {}

    /**
     * Create an instance of the given value
     *
     * @since    0.2.0
     */
    public static BvDouble of(Double value) {
        return new BvDouble(value);
    }

    /**
     * Get the identifier of this type
     *
     * @since    0.2.0
     */
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

        if (nbt_value instanceof AbstractNbtNumber nbt_nr) {
            this.setContainedValue(nbt_nr.doubleValue());
        }
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtElement toNbt() {

        Double contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        return NbtDouble.of(this.contained_value);
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

        this.setContainedValue(json_prim.getAsDouble());
    }

    /**
     * Cast the given Number to our own contained type
     *
     * @since    0.2.0
     */
    @Override
    protected Double castToOurContainedNumber(Number nr) {

        if (nr == null) {
            return null;
        }

        return (Double) nr;
    }
}
