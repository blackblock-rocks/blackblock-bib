package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibJson;

import java.util.Objects;

/**
 * Represents a BV type
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class
AbstractBvType<ContainedType, OwnType extends BvElement<?, ?>>
    implements BvElement<ContainedType, OwnType> {

    // The contained value
    protected ContainedType contained_value = null;

    /**
     * Get the actual underlying Java value
     *
     * @since    0.1.0
     */
    @Override
    public ContainedType getContainedValue() {
        return this.contained_value;
    }

    /**
     * Get the actual underlying Java value
     *
     * @since    0.1.0
     */
    @Override
    public void setContainedValue(ContainedType value) {
        this.contained_value = value;
    }

    /**
     * Do simple equals check
     *
     * @since    0.1.0
     */
    @Override
    public boolean equalsOtherValue(OwnType other_instance) {

        ContainedType own_value = this.getContainedValue();
        Object other_value = other_instance.getContainedValue();

        return Objects.equals(own_value, other_value);
    }

    /**
     * Set a default `equals()` implementation:
     * this will cover most cases
     *
     * @since    0.1.0
     */
    public boolean equals(Object object) {

        // Simple reference check
        if (this == object) {
            return true;
        }

        // Null check
        if (object == null) {
            return false;
        }

        // Check the type
        if (this.getClass() != object.getClass()) {
            return false;
        }

        return this.equalsOtherValue((OwnType) object);
    }

    /**
     * Serialize this value to JSON
     *
     * @since    0.1.0
     */
    @Override
    @Nullable
    public JsonElement toJson() {

        NbtElement nbt_value = this.toNbt();

        if (nbt_value == null) {
            return null;
        }

        return BibJson.jsonify(nbt_value);
    }
}
