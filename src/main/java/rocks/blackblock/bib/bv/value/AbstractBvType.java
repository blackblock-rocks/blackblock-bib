package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibJson;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a BV type
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class
AbstractBvType<ContainedType, OwnType extends BvElement<?, ?>>
    implements BvElement<ContainedType, OwnType>, BibLog.Argable {

    // The default item to use to represent this element inside an GUI
    public static Item DEFAULT_ICON_ITEM = Items.BARRIER;

    // The item to use when representing the value in an ItemStack
    public static Item VALUE_ITEM = Items.PAPER;

    // The contained value
    protected ContainedType contained_value = null;

    // All the tags this element might have
    protected Set<BvElement> tags = null;

    /**
     * Get the actual underlying Java value
     *
     * @since    0.2.0
     */
    @Override
    public ContainedType getContainedValue() {
        return this.contained_value;
    }

    /**
     * Get the actual underlying Java value
     *
     * @since    0.2.0
     */
    @Override
    public void setContainedValue(ContainedType value) {
        this.contained_value = value;
    }

    /**
     * Get all the tags of this element
     *
     * @since    0.2.0
     */
    @Override
    public Set<BvElement> getTags() {
        return this.tags;
    }

    /**
     * Add a tag to this element
     *
     * @since    0.2.0
     */
    @Override
    public void addTag(BvElement tag) {

        if (this.tags == null) {
            this.tags = new HashSet<>();
        }

        this.tags.add(tag);
    }

    /**
     * Remove all the tags of this element
     *
     * @since    0.2.0
     */
    @Override
    public void clearTags() {
        this.tags = null;
    }

    /**
     * Do simple equals check
     *
     * @since    0.2.0
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
     * @since    0.2.0
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
     * @since    0.2.0
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

    /**
     * Return a Arg instance
     *
     * @since    0.2.0
     */
    @Override
    public BibLog.Arg toBBLogArg() {

        BibLog.Arg result = BibLog.createArg(this);

        result.add("type", this.getType());

        ContainedType val = this.getContainedValue();

        if (val != this) {
            result.add("value", val);
        }

        return result;
    }

    /**
     * Return a string representation
     *
     * @since    0.2.0
     */
    @Override
    public String toString() {
        return this.toBBLogArg().toString();
    }
}
