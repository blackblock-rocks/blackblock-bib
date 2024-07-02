package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Message;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.bv.operator.BvOperator;
import rocks.blackblock.bib.bv.operator.BvOperators;
import rocks.blackblock.bib.util.BibItem;
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
@SuppressWarnings({
        // Ignore unused warnings: this is a library after all
        "unused",
        
        // Ignore warnings of using raw types
        "rawtypes",
        
        // Ignore unchecked typecast warnings
        "unchecked"
})
public interface BvElement<ContainedType, OwnType extends BvElement<?, ?>> {

    // The type registry
    Map<String, Supplier<BvElement>> TYPE_REGISTRY = new HashMap<>();
    Map<String, Class<? extends BvElement>> CLASS_REGISTRY = new HashMap<>();
    Map<Class<? extends BvElement>, Supplier<BvElement>> CLASS_TO_SUPPLIER = new HashMap<>();

    Supplier<BvBoolean> BOOLEAN_SUPPLIER = registerType(BvBoolean.TYPE, BvBoolean.class, BvBoolean::new);
    Supplier<BvInteger> INTEGER_SUPPLIER = registerType(BvInteger.TYPE, BvInteger.class, BvInteger::new);
    Supplier<BvMap> MAP_SUPPLIER = registerType(BvMap.TYPE, BvMap.class, BvMap::new);
    Supplier<BvString> STRING_SUPPLIER = BvElement.registerType(BvString.TYPE, BvString.class, BvString::new);
    Supplier<BvNull> NULL_SUPPLIER = BvElement.registerType(BvNull.TYPE, BvNull.class, () -> BvNull.NULL);
    Supplier<BvNumber> NUMBER_SUPPLIER = BvElement.registerType("number", BvNumber.class, BvDouble::new);

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
     * Get a string value to use in a placeholder
     *
     * @since    0.1.0
     */
    String toPlaceholderString();

    /**
     * Convert to a string for use in commands
     *
     * @since    0.1.0
     */
    @NotNull
    default String toCommandString() {
        return this.toPlaceholderString();
    }

    /**
     * Old name of toCommandString
     */
    @Deprecated
    default String toPlaceholderStringForCommand() {
        return this.toPlaceholderString();
    }

    /**
     * Convert to a tooltip for use in commands
     *
     * @since    0.1.0
     */
    @Nullable
    default Message toCommandTooltip() {
        return this.toPrettyText();
    }

    /**
     * Set the value, and return if it worked
     *
     * @since    0.1.0
     */
    default boolean setValue(Object value) {

        if (value == null) {
            this.setContainedValue(null);
            return true;
        }

        ContainedType contained_type_value;

        // First see if it's a flow value
        if (value instanceof BvElement<?,?> bv_value) {

            try {
                contained_type_value = (ContainedType) (bv_value.getContainedValue());
                this.setContainedValue(contained_type_value);
            } catch (Exception e) {
                BibLog.log("Unable to setValue", this, "and", value, "are not the same type");
                return false;
            }

            return true;
        }

        try {
            contained_type_value = (ContainedType) value;
            this.setContainedValue(contained_type_value);
        } catch (Exception e) {
            BibLog.log("Unable to setValue", this, "can not contain", value);
            return false;
        }

        return true;
    }

    /**
     * Get the title of this value (for visual stuff)
     *
     * @since    0.1.0
     */
    default String getDisplayTitle() {
        return this.toPlaceholderString();
    }

    /**
     * Get the item to use as an icon for this value,
     * in case it needs to be shown in an inventory
     *
     * @since    0.1.0
     */
    default Item getItemIcon() {
        // @TODO: Make blackblock-core register BBSB.GUI_UNKOWN_TYPE for this!
        return null;
    }

    /**
     * Create an ItemStack representation of this value
     *
     * @since    0.1.0
     */
    default ItemStack createIconStack() {

        Item item = this.getItemIcon();

        if (item == null) {
            item = Items.BARRIER;
        }

        ItemStack stack = new ItemStack(item);
        String title = this.getDisplayTitle();
        BibItem.setCustomName(stack, Text.literal(title).setStyle(Style.EMPTY.withItalic(false)));

        return stack;
    }

    /**
     * Get an operator by name
     *
     * @since    0.1.0
     */
    default BvOperator<? extends BvElement> getOperator(String operator_name) {
        Class<? extends BvElement> constructor = this.getClass();
        return BvOperators.getOperator(constructor, operator_name);
    }

    /**
     * Execute a unary operator that does not have an executor
     *
     * @since    0.1.0
     */
    default Boolean executeCustomUnaryOperator(BvOperator operator) {
        return null;
    }

    /**
     * Execute a binary operator that does not have an executor
     *
     * @since    0.1.0
     */
    default Boolean executeCustomBinaryOperator(BvOperator operator, BvElement right) {
        return null;
    }

    /**
     * Execute a ternary operator that does not have an executor
     *
     * @since    0.1.0
     */
    default Boolean executeCustomTernaryOperator(BvOperator operator, BvElement mid, BvElement right) {
        return null;
    }

    /**
     * Register a BvElement type
     *
     * @since    0.1.0
     */
    static <T extends BvElement> Supplier<T> registerType(String type, Class<T> type_class, Supplier<T> supplier) {
        TYPE_REGISTRY.put(type, (Supplier<BvElement>) supplier);
        CLASS_REGISTRY.put(type, type_class);
        CLASS_TO_SUPPLIER.put(type_class, (Supplier<BvElement>) supplier);
        return supplier;
    }

    /**
     * Get the class of the given type
     *
     * @since    0.1.0
     *
     * @param    type    The type identifier of the value
     */
    @Nullable
    static Class<? extends BvElement> getValueClass(String type) {
        return CLASS_REGISTRY.get(type);
    }

    /**
     * Get a new BvElement instance by its type class
     *
     * @since    0.1.0
     */
    @Nullable
    static <T extends BvElement> T createNewOfType(Class<T> type_class) {

        Supplier<BvElement> supplier = CLASS_TO_SUPPLIER.get(type_class);

        if (supplier == null) {
            return null;
        }

        return (T) supplier.get();
    }

    /**
     * Get a new BvElement instance by its type name
     *
     * @since    0.1.0
     */
    @Nullable
    static BvElement createNewOfType(String type) {

        Supplier<BvElement> supplier = TYPE_REGISTRY.get(type);

        if (supplier == null) {
            return null;
        }

        return supplier.get();
    }

    /**
     * Parse the given JSON element
     *
     * @since    0.1.0
     */
    @Nullable
    static BvElement parseFromJson(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (!(element instanceof JsonObject compound)) {
            return null;
        }

        JsonElement $type = compound.get("$type");

        if ($type == null || !$type.isJsonPrimitive() || $type.isJsonNull()) {
            return null;
        }

        String type = $type.getAsString();

        BvElement instance = BvElement.createNewOfType(type);

        if (instance == null) {
            return null;
        }

        JsonElement $data;

        if (compound.has("$data")) {
            $data = compound.get("$data");
        } else if (compound.has("$value")) {
            $data = compound.get("$value");
        } else {
            $data = null;
        }

        instance.loadFromJson($data);

        return instance;
    }

    /**
     * Serialize the given element to JSON
     *
     * @since    0.1.0
     */
    @Nullable
    static JsonObject serializeToJson(BvElement element) {

        if (element == null) {
            return null;
        }

        JsonElement element_json = element.toJson();

        if (element_json == null) {
            return null;
        }

        JsonObject compound = new JsonObject();
        compound.addProperty("$type", element.getType());
        compound.add("$data", element_json);

        return compound;
    }

    /**
     * Parse the given NBT
     *
     * @since    0.1.0
     */
    @Nullable
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
