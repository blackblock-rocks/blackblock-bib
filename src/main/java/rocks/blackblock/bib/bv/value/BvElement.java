package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a BV type
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public interface BvElement<ContainedType, OwnType extends BvElement<?, ?>> {

    // The type registry
    Map<String, Supplier<BvElement>> TYPE_REGISTRY = new HashMap<>();
    Supplier<BvBoolean> BOOLEAN_SUPPLIER = registerType(BvBoolean.TYPE, BvBoolean::new);
    Supplier<BvInteger> INTEGER_SUPPLIER = registerType(BvInteger.TYPE, BvInteger::new);
    Supplier<BvMap> MAP_SUPPLIER = registerType(BvMap.TYPE, BvMap::new);
    Supplier<BvString> STRING_SUPPLIER = BvElement.registerType(BvString.TYPE, BvString::new);

    /**
     * Get the actual underlying Java value
     *
     * @since    0.1.0
     */
    ContainedType getContainedValue();

    /**
     * Set the actual underlying Java value
     *
     * @since    0.1.0
     */
    void setContainedValue(ContainedType value);

    /**
     * Check two instances of the same type
     *
     * @since    0.1.0
     */
    boolean equalsOtherValue(OwnType object);

    /**
     * Get the identifier of this type
     *
     * @since    0.1.0
     */
    String getType();

    /**
     * Load this value from NBT
     *
     * @since    0.1.0
     */
    void loadFromNbt(NbtElement nbt_value);

    /**
     * Serialize this value to NBT
     *
     * @since    0.1.0
     */
    @Nullable
    NbtElement toNbt();

    /**
     * Load the value from JSON
     *
     * @since    0.1.0
     */
    void loadFromJson(JsonElement json);

    /**
     * Serialize this value to JSON
     *
     * @since    0.1.0
     */
    @Nullable
    JsonElement toJson();

    /**
     * Convert to pretty text
     *
     * @since    0.1.0
     */
    @Nullable
    Text toPrettyText();

    /**
     * Register a BvElement type
     *
     * @since    0.1.0
     */
    static <T extends BvElement> Supplier<T> registerType(String type, Supplier<T> supplier) {
        TYPE_REGISTRY.put(type, (Supplier<BvElement>) supplier);
        return supplier;
    }

    /**
     * Get a new BvElement instance by its type name
     *
     * @since    0.1.0
     */
    static BvElement createNewOfType(String type) {

        Supplier<BvElement> supplier = TYPE_REGISTRY.get(type);

        if (supplier == null) {
            return null;
        }

        return supplier.get();
    }

    /**
     * Parse the given NBT
     *
     * @since    0.1.0
     */
    static BvElement parseFromNbt(NbtElement nbt) {

        if (nbt == null) {
            return null;
        }

        if (!(nbt instanceof NbtCompound compound)) {
            return null;
        }

        if (!compound.contains("$type", NbtElement.STRING_TYPE)) {
            return null;
        }

        String type = compound.getString("$type");

        BvElement instance = BvElement.createNewOfType(type);

        if (instance == null) {
            return null;
        }

        NbtElement data_nbt = compound.get("$data");
        instance.loadFromNbt(data_nbt);

        return instance;
    }

    /**
     * Serialize the given element to NBT
     *
     * @since    0.1.0
     */
    static NbtCompound serializeToNbt(BvElement element) {

        if (element == null) {
            return null;
        }

        NbtElement element_nbt = element.toNbt();

        if (element_nbt == null) {
            return null;
        }

        NbtCompound compound = new NbtCompound();
        compound.putString("$type", element.getType());
        compound.put("$data", element_nbt);

        return compound;
    }

    /**
     * Get a pretty text representation
     *
     * @since    0.1.0
     */
    @NotNull
    static Text getPrettyText(@Nullable BvElement element) {

        Text result;

        if (element == null) {
            result = null;
        } else {
            result = element.toPrettyText();
        }

        if (result == null) {
            result = Text.literal("null").formatted(Formatting.GRAY);
        }

        return result;
    }
}
