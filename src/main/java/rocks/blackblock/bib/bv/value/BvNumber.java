package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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
     * Create an instance of the given value
     *
     * @since    0.1.0
     */
    public static BvNumber of(Number value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Integer int_nr) {
            return new BvInteger(int_nr);
        }

        return new BvDouble((double) value);
    }

    /**
     * Convert to a JSON value
     *
     * @since    0.1.0
     */
    @Override
    public JsonElement toJson() {

        Number value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        return new JsonPrimitive(value);
    }

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

    /**
     * Return the current number for use in a placeholder
     *
     * @since    0.1.0
     */
    @Override
    public String toPlaceholderString() {
        return "" + this.contained_value;
    }

    /**
     * Cast the given Number to our own contained type
     *
     * @since    0.5.0
     */
    abstract protected ContainedType castToOurContainedNumber(Number nr);

    /**
     * Set the number directly
     *
     * @since    0.5.0
     */
    @Override
    public void setContainedValue(Number nr) {

        if (nr == null) {
            this.contained_value = null;
            return;
        }

        this.contained_value = this.castToOurContainedNumber(nr);
    }

    /**
     * Get our contained value as a double.
     * Null will be returned as 0
     *
     * @since    0.5.0
     */
    public double getDoubleValue() {
        if (this.contained_value == null) {
            return 0;
        }

        return this.contained_value.doubleValue();
    }

    /**
     * Add the given value
     *
     * @since    0.5.0
     */
    public void add(BvNumber<?, ?> nr) {
        this.add(nr.getContainedValue());
    }

    /**
     * Add the given value
     *
     * @since    0.5.0
     */
    public void add(Number nr) {

        if (nr == null) {
            return;
        }

        this.setContainedValue(this.getDoubleValue() + nr.doubleValue());
    }

    /**
     * Subtract the given value
     *
     * @since    0.5.0
     */
    public void subtract(BvNumber<?, ?> nr) {
        this.subtract(nr.getContainedValue());
    }

    /**
     * Subtract the given value
     *
     * @since    0.5.0
     */
    public void subtract(Number nr) {

        if (nr == null) {
            return;
        }

        this.setContainedValue(this.getDoubleValue() - nr.doubleValue());
    }

    /**
     * Multiply the given value
     *
     * @since    0.5.0
     */
    public void multiply(BvNumber<?, ?> nr) {
        this.multiply(nr.getContainedValue());
    }

    /**
     * Multiply the given value
     *
     * @since    0.5.0
     */
    public void multiply(Number nr) {

        if (nr == null) {
            return;
        }

        this.setContainedValue(this.getDoubleValue() * nr.doubleValue());
    }

    /**
     * Divide the given value
     *
     * @since    0.5.0
     */
    public void divide(BvNumber<?, ?> nr) {
        this.divide(nr.getContainedValue());
    }

    /**
     * Divide the given value
     *
     * @since    0.5.0
     */
    public void divide(Number nr) {

        if (nr == null) {
            return;
        }

        this.setContainedValue(this.getDoubleValue() / nr.doubleValue());
    }

    /**
     * Modulo the given value
     *
     * @since    0.5.0
     */
    public void modulo(BvNumber<?, ?> nr) {
        this.modulo(nr.getContainedValue());
    }

    /**
     * Modulo the given value
     *
     * @since    0.5.0
     */
    public void modulo(Number nr) {

        if (nr == null) {
            return;
        }

        this.setContainedValue(this.getDoubleValue() % nr.doubleValue());
    }

    /**
     * Get the floored integer value of this number
     * The value is always floored
     *
     * @since    0.5.0
     */
    public int getFlooredInteger() {
        return (int) Math.floor(this.getDoubleValue());
    }

    /**
     * Try to return the numerical value
     *
     * @since    0.1.0
     */
    public static Number getNumberValue(BvElement<?, ?> value) {

        if (value instanceof BvNumber<?,?> nr) {
            return nr.getContainedValue();
        }

        if (value instanceof BvBoolean bool) {
            return bool.getContainedValue() ? 1 : 0;
        }

        return null;
    }
}
