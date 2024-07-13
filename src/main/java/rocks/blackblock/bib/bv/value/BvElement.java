package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Message;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.bv.operator.BvOperator;
import rocks.blackblock.bib.bv.operator.BvOperators;
import rocks.blackblock.bib.interfaces.HasItemIcon;
import rocks.blackblock.bib.util.BibItem;
import rocks.blackblock.bib.util.BibLog;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a BV type
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings({
        // Ignore unused warnings: this is a library after all
        "unused",
        
        // Ignore warnings of using raw types
        "rawtypes",
        
        // Ignore unchecked typecast warnings
        "unchecked"
})
public interface BvElement<ContainedType, OwnType extends BvElement<?, ?>> extends HasItemIcon {

    BibLog.Categorised LOGGER = BibLog.getCategorised("bv");

    // The type registry
    Map<String, Supplier<BvElement>> TYPE_REGISTRY = new HashMap<>();
    Map<String, Class<? extends BvElement>> CLASS_REGISTRY = new HashMap<>();
    Map<Class<? extends BvElement>, Supplier<BvElement>> CLASS_TO_SUPPLIER = new HashMap<>();
    Map<Class<? extends BvElement>, Function<NbtElement, BvElement>> CLASS_TO_REVIVER = new HashMap<>();

    // Suppliers of empty instances
    Supplier<BvBoolean> BOOLEAN_SUPPLIER = registerType(BvBoolean.TYPE, BvBoolean.class, BvBoolean::new);
    Supplier<BvInteger> INTEGER_SUPPLIER = registerType(BvInteger.TYPE, BvInteger.class, BvInteger::new);
    Supplier<BvList> LIST_SUPPLIER = BvElement.registerType(BvList.TYPE, BvList.class, BvList::new);
    Supplier<BvMap> MAP_SUPPLIER = registerType(BvMap.TYPE, BvMap.class, BvMap::new);
    Supplier<BvString> STRING_SUPPLIER = BvElement.registerType(BvString.TYPE, BvString.class, BvString::new);
    Supplier<BvNull> NULL_SUPPLIER = BvElement.registerType(BvNull.TYPE, BvNull.class, () -> BvNull.NULL);
    Supplier<BvNumber> NUMBER_SUPPLIER = BvElement.registerType("number", BvNumber.class, BvDouble::new);
    Supplier<BvLootTableSet> LOOT_TABLE_SET_SUPPLIER = BvElement.registerType(BvLootTableSet.TYPE, BvLootTableSet.class, BvLootTableSet::new, BvLootTableSet::fromNbt);
    Supplier<BvTag> TAG_SUPPLIER = BvElement.registerType(BvTag.TYPE, BvTag.class, BvTag::createUnsafeEmptyTag);

    /**
     * Get the actual underlying Java value
     *
     * @since    0.2.0s
     */
    ContainedType getContainedValue();

    /**
     * Set the actual underlying Java value
     *
     * @since    0.2.0
     */
    void setContainedValue(ContainedType value);

    /**
     * Check two instances of the same type
     *
     * @since    0.2.0
     */
    boolean equalsOtherValue(OwnType object);

    /**
     * Get the identifier of this type
     *
     * @since    0.2.0
     */
    String getType();

    /**
     * Load this value from NBT
     *
     * @since    0.2.0
     */
    void loadFromNbt(NbtElement nbt_value);

    /**
     * Serialize this value to NBT
     *
     * @since    0.2.0
     */
    @Nullable
    NbtElement toNbt();

    /**
     * Load the value from JSON
     *
     * @since    0.2.0
     */
    void loadFromJson(JsonElement json);

    /**
     * Serialize this value to JSON
     *
     * @since    0.2.0
     */
    @Nullable
    JsonElement toJson();

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @Nullable
    Text toPrettyText();

    /**
     * Get a string value to use in a placeholder
     *
     * @since    0.2.0
     */
    String toPlaceholderString();

    /**
     * Get all the tags of this element
     *
     * @since    0.2.0
     */
    Set<BvElement> getTags();

    /**
     * Check if this element has the given tag.
     * Takes a second parameter to prevent infinite loops.
     *
     * @since    0.2.0
     */
    private static boolean internalCheckForTags(Collection<BvElement> wanted_tags, Collection<BvElement> our_tags, Set<BvElement> seen) {

        boolean result = true;

        // Look for each wanted tag
        for (BvElement wanted_tag : wanted_tags) {

            // If the tag is explicitly present, the result can stay true
            if (our_tags.contains(wanted_tag)) {
                continue;
            }

            boolean found_in_parent = false;

            // We did not find the wanted tag,
            // so we need to see if any of OUR tags' parent tags contain it
            for (BvElement our_tag : our_tags) {

                Set<BvElement> parent_tags = our_tag.getTags();

                if (parent_tags == null) {
                    continue;
                }

                if (parent_tags.contains(wanted_tag)) {
                    found_in_parent = true;
                    break;
                }

                // If we've already seen this tag, skip it
                if (seen.contains(wanted_tag)) {
                    break;
                }

                seen.add(wanted_tag);

                // Check the parent tags of this tag
                if (internalCheckForTags(wanted_tags, parent_tags, seen)) {
                    found_in_parent = true;
                    break;
                }
            }

            if (!found_in_parent) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Is this element tagged with the given tags?
     * Will also check parent tags.
     *
     * @since    0.2.0
     */
    default boolean hasTags(Collection<BvElement> wanted_tags) {

        if (wanted_tags == null || wanted_tags.isEmpty()) {
            return true;
        }

        Set<BvElement> our_tags = this.getTags();

        if (our_tags == null) {
            return false;
        }

        if (our_tags.containsAll(wanted_tags)) {
            return true;
        }

        return internalCheckForTags(wanted_tags, our_tags, new HashSet<>());
    }

    /**
     * Is this element tagged with the given tag?
     *
     * @since    0.2.0
     */
    default boolean hasTag(BvElement tag) {
        return this.hasTags(Set.of(tag));
    }

    /**
     * Add a tag to this element
     *
     * @since    0.2.0
     */
    void addTag(BvElement tag);

    /**
     * Remove all tags from this element
     *
     * @since    0.2.0
     */
    void clearTags();

    /**
     * Convert to a string for use in commands
     *
     * @since    0.2.0
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
     * @since    0.2.0
     */
    @Nullable
    default Message toCommandTooltip() {
        return this.toPrettyText();
    }

    /**
     * Set the value, and return if it worked
     *
     * @since    0.2.0
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
     * @since    0.2.0
     */
    default String getDisplayTitle() {
        return this.toPlaceholderString();
    }

    /**
     * Get the title of this value as Text
     *
     * @since    0.2.0
     */
    default Text getDisplayTitleText() {
        return Text.literal(this.getDisplayTitle());
    }

    /**
     * Get optional description to use.
     * This will be used as the item stack's lore
     *
     * @since 0.2.0
     */
    @Nullable
    default List<Text> getDisplayDescription() {
        return null;
    }

    /**
     * Get the item to use as an icon for this value,
     * in case it needs to be shown in an inventory
     *
     * @since    0.2.0
     */
    @Override
    default Item getItemIcon() {
        return AbstractBvType.DEFAULT_ICON_ITEM;
    }

    /**
     * Get the item to use when serializing to an ItemStack
     *
     * @since    0.2.0
     */
    default Item getItemForValue() {
        return AbstractBvType.VALUE_ITEM;
    }

    /**
     * Create an ItemStack representation of this value for use as an icon
     * Similar to {@link #createValueStack}, except that it uses the icon item
     * and the actual value does not get serialized to the ItemStack's NBT data.
     *
     * @since    0.2.0
     */
    default ItemStack createIconStack() {

        Item item = this.getItemIcon();

        if (item == null || item == Items.AIR) {
            item = Items.BARRIER;
        }

        ItemStack stack = new ItemStack(item);
        BibItem.setCustomName(stack, this.getDisplayTitleText());
        BibItem.replaceLore(stack, this.getDisplayDescription());

        return stack;
    }

    /**
     * Create an ItemStack representation of this value.
     * This ItemStack can be used in the actual world.
     *
     * @since    0.2.0
     */
    default ItemStack createValueStack() {

        Item item = this.getItemForValue();

        if (item == null || item == Items.AIR) {
            item = Items.PAPER;
        }

        ItemStack stack = new ItemStack(item);
        BibItem.setCustomName(stack, Text.literal(this.getType() + ": ").append(this.getDisplayTitleText()));
        BibItem.replaceLore(stack, this.getDisplayDescription());

        NbtCompound nbt = BvElement.serializeToNbt(this);
        BibItem.setCustomNbt(stack, nbt);

        return stack;
    }

    /**
     * Get an operator by name
     *
     * @since    0.2.0
     */
    default BvOperator<? extends BvElement> getOperator(String operator_name) {
        Class<? extends BvElement> constructor = this.getClass();
        return BvOperators.getOperator(constructor, operator_name);
    }

    /**
     * Execute a unary operator that does not have an executor
     *
     * @since    0.2.0
     */
    default Boolean executeCustomUnaryOperator(BvOperator operator) {
        return null;
    }

    /**
     * Execute a binary operator that does not have an executor
     *
     * @since    0.2.0
     */
    default Boolean executeCustomBinaryOperator(BvOperator operator, BvElement right) {
        return null;
    }

    /**
     * Execute a ternary operator that does not have an executor
     *
     * @since    0.2.0
     */
    default Boolean executeCustomTernaryOperator(BvOperator operator, BvElement mid, BvElement right) {
        return null;
    }

    /**
     * Register a BvElement type
     *
     * @since    0.2.0
     */
    static <T extends BvElement> Supplier<T> registerType(String type, Class<T> type_class, Supplier<T> supplier) {
        return registerType(type, type_class, supplier, null);
    }

    /**
     * Register a BvElement type
     *
     * @since    0.2.0
     */
    static <T extends BvElement> Supplier<T> registerType(String type, Class<T> type_class, Supplier<T> supplier, Function<NbtElement, T> reviver) {
        TYPE_REGISTRY.put(type, (Supplier<BvElement>) supplier);
        CLASS_REGISTRY.put(type, type_class);
        CLASS_TO_SUPPLIER.put(type_class, (Supplier<BvElement>) supplier);

        if (reviver != null) {
            CLASS_TO_REVIVER.put(type_class, (Function<NbtElement, BvElement>) reviver);
        }

        if (LOGGER.isEnabled()) {
            LOGGER.log("Registered type", type, "as", type_class);
        }

        return supplier;
    }

    /**
     * Get the class of the given type
     *
     * @since    0.2.0
     *
     * @param    type    The type identifier of the value
     */
    @Nullable
    static Class<? extends BvElement> getValueClass(String type) {
        return CLASS_REGISTRY.get(type);
    }

    /**
     * Revive an element using its custom registered static reviver
     *
     * @since    0.2.0
     */
    @Nullable
    static <T extends BvElement> T reviveOfType(String $type, NbtElement nbt) {

        Class<? extends BvElement> type_class = CLASS_REGISTRY.get($type);

        if (type_class == null) {
            return null;
        }

        Function<NbtElement, BvElement> reviver = CLASS_TO_REVIVER.get(type_class);

        if (reviver == null) {
            return null;
        }

        return (T) reviver.apply(nbt);
    }

    /**
     * Get a new BvElement instance by its type class
     *
     * @since    0.2.0
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
     * @since    0.2.0
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
     * @since    0.2.0
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
     * Parse the given JSON data and expect the given type
     *
     * @since    0.2.0
     */
    @Nullable
    static <T extends BvElement> T parseFromJson(JsonElement element, @NotNull Class<T> expected_type_class) {

        BvElement result = parseFromJson(element);

        if (!expected_type_class.isInstance(result)) {
            return null;
        }

        return (T) result;
    }

    /**
     * Parse the given JSON data and expect a list with the given type
     *
     * @since    0.2.0
     */
    @Nullable
    static <T extends BvElement> BvList<T> parseListFromJson(JsonElement element, @NotNull Class<T> expected_type_class) {

        BvElement parsed_element = parseFromJson(element);

        if (!(parsed_element instanceof BvList list)) {
            return null;
        }

        if (!list.containsType(expected_type_class)) {
            return null;
        }

        return list;
    }

    /**
     * Serialize the given element to JSON
     *
     * @since    0.2.0
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
     * @since    0.2.0
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
        NbtElement data_nbt = compound.get("$data");

        // Try to revive an instance first
        BvElement instance = BvElement.reviveOfType(type, data_nbt);

        if (instance != null) {
            return instance;
        }

        // Try to create a new instance
        instance = BvElement.createNewOfType(type);

        if (instance == null) {
            return null;
        }

        instance.loadFromNbt(data_nbt);

        return instance;
    }

    /**
     * Parse the given NBT data and expect the given type
     *
     * @since    0.2.0
     */
    @Nullable
    static <T extends BvElement> T parseFromNbt(NbtElement nbt, @NotNull Class<T> expected_type_class) {

        BvElement result = parseFromNbt(nbt);

        if (!expected_type_class.isInstance(result)) {
            return null;
        }

        return (T) result;
    }

    /**
     * Parse the given NBT data and expect a list with the given type
     *
     * @since    0.2.0
     */
    @Nullable
    static <T extends BvElement> BvList<T> parseListFromNbt(NbtElement nbt, @NotNull Class<T> expected_type_class) {

        BvElement parsed_element = parseFromNbt(nbt);

        if (!(parsed_element instanceof BvList list)) {
            return null;
        }

        if (!list.containsType(expected_type_class)) {
            return null;
        }

        return list;
    }

    /**
     * Serialize the given element to NBT
     *
     * @since    0.2.0
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
     * @since    0.2.0
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
