package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import org.jetbrains.annotations.Nullable;

/**
 * A BV Integer
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvInteger extends BvNumber<Integer, BvInteger> {

    public static final String TYPE = "integer";

    /**
     * Construct an integer with a pre-defined value
     *
     * @since    0.2.0
     */
    public BvInteger(Integer value) {
        this.setContainedValue(value);
    }

    /**
     * Construct a BvInteger with no value
     *
     * @since    0.2.0
     */
    public BvInteger() {}

    /**
     * Create an instance of the given value
     *
     * @since    0.2.0
     */
    public static BvInteger of(Integer value) {
        return new BvInteger(value);
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

        if (nbt_value instanceof NbtInt nbt_int) {
            this.setContainedValue(nbt_int.intValue());
        }
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtElement toNbt() {

        Integer contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        return NbtInt.of(this.contained_value);
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

        this.setContainedValue(json_prim.getAsInt());
    }

    /**
     * Cast the given Number to our own contained type
     *
     * @since    0.2.0
     */
    @Override
    protected Integer castToOurContainedNumber(Number nr) {

        if (nr == null) {
            return null;
        }

        return (Integer) nr;
    }
}
