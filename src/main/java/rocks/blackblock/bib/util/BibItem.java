package rocks.blackblock.bib.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.BibMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Library class for working with Items & ItemStacks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibItem {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibItem() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get the identifier of an item
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Identifier getIdentifierOf(Item item) {
        return Registries.ITEM.getId(item);
    }

    /**
     * Get an item by its identifier
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Item getByIdentifier(Identifier identifier) {
        return Registries.ITEM.get(identifier);
    }

    /**
     * Get an optional item by its identifier
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Optional<Item> getByIdentifierOrEmpty(Identifier identifier) {
        return Registries.ITEM.getOrEmpty(identifier);
    }

    /**
     * Create new ItemSettings
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Item.Settings createItemSettings() {
        return new Item.Settings();
    }

    /**
     * Does the given stack have custom NBT data?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasCustomNbt(ItemStack stack) {

        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (data == null) {
            return false;
        }

        return !data.isEmpty();
    }

    /**
     * Get or create the custom NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtCompound getCustomNbt(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (data == null) {
            data = NbtComponent.of(new NbtCompound());
            stack.set(DataComponentTypes.CUSTOM_DATA, data);
        }

        return data.getNbt();
    }

    /**
     * Set the custom NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setCustomNbt(ItemStack stack, NbtCompound nbt) {
        NbtComponent data = NbtComponent.of(nbt);
        stack.set(DataComponentTypes.CUSTOM_DATA, data);
    }

    /**
     * Create an ItemStack of the given Item and
     * set the given CustomModelData integer value on it
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack createStackWithCustomModelData(Item item, int custom_model_data_value) {
        return BibItem.setCustomModelDataValue(new ItemStack(item), custom_model_data_value);
    }

    /**
     * Set the CustomModelData value of a stack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack setCustomModelDataValue(ItemStack stack, int custom_model_data_value) {

        var cmd_component = new CustomModelDataComponent(custom_model_data_value);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, cmd_component);

        return stack;
    }

    /**
     * Set the custom name of the given stack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setCustomName(ItemStack stack, Text text) {
        stack.set(DataComponentTypes.CUSTOM_NAME, text);
    }

    /**
     * Append the given text to the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void appendLore(ItemStack stack, Text text) {
        var lore_component = stack.getOrDefault(DataComponentTypes.LORE, new LoreComponent(List.of()));

        List<Text> list = lore_component.lines();
        list.add(text);
    }

    /**
     * Replace the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void replaceLore(ItemStack stack, List<? extends Text> lines) {
        List<Text> simplified_lines = new ArrayList<>(lines);
        stack.set(DataComponentTypes.LORE, new LoreComponent(simplified_lines));
    }

    /**
     * Replace the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void replaceLore(ItemStack stack, Text text) {
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(text)));
    }

    /**
     * Can the 2 stacks be combined?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean canCombine(ItemStack left, ItemStack right) {
        return ItemStack.areItemsAndComponentsEqual(left, right);
    }

    /**
     * Are the 2 item stacks equal, ignoring damage?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean areEqualIgnoreDamage(ItemStack left, ItemStack right) {

        if (left.getItem() != right.getItem()) {
            return false;
        }

        if (left.isDamageable()) {
            left = left.copy();
            right = right.copy();

            left.setDamage(0);
            right.setDamage(0);
        }

        return Objects.equals(left.getComponents(), right.getComponents());
    }

    /**
     * Serialize an ItemStack into an NbtElement
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null; !null -> !null")
    public static NbtElement serializeStack(ItemStack stack) {

        if (stack == null) {
            return null;
        }

        return stack.encode(BibMod.getDynamicRegistry());
    }

    /**
     * Deserialize an NbtElement into an ItemStack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static ItemStack deserializeToStack(NbtElement nbt) {
        return ItemStack.fromNbt(BibMod.getDynamicRegistry(), nbt).orElse(null);
    }

    /**
     * Get the Item NbtList of a container.
     * Will also check BlockEntity tags.
     *
     * @TODO: BlockEntityTag has changed in 1.20.5
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static NbtList getItemContainerList(ItemStack stack) {

        if (stack == null || stack.isEmpty() || stack.getCount() > 1) {
            return null;
        }

        NbtCompound nbt = BibItem.getCustomNbt(stack);

        if (nbt == null || nbt.isEmpty()) {
            return null;
        }

        if (nbt.contains("BlockEntityTag")) {
            nbt = nbt.getCompound("BlockEntityTag");
        }

        if (nbt == null || !nbt.contains("Items")) {
            return null;
        }

        return nbt.getList("Items", NbtElement.COMPOUND_TYPE);
    }

    /**
     * Is the given itemstack a container of items?
     * (Will only be true when it actually contains items)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean isItemContainer(ItemStack stack) {

        NbtList item_list = BibItem.getItemContainerList(stack);

        if (item_list == null || item_list.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Extract items stored in the itemstack's nbt data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static List<ItemStack> extractItems(ItemStack stack) {

        NbtList item_list = getItemContainerList(stack);

        if (item_list == null || item_list.isEmpty()) {
            return null;
        }

        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < item_list.size(); ++i) {
            NbtCompound nbtCompound = item_list.getCompound(i);
            ItemStack slot_stack = ItemStack.fromNbt(DynamicRegistryManager.EMPTY, nbtCompound).orElse(null);

            if (slot_stack != null && !slot_stack.isEmpty()) {
                items.add(slot_stack);
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        return items;
    }

    /**
     * Write the list of items to the itemstack nbt
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void insertItems(ItemStack stack, List<ItemStack> stacks) {

        NbtCompound nbt = BibItem.getCustomNbt(stack);

        if (nbt.contains("BlockEntityTag")) {
            nbt = nbt.getCompound("BlockEntityTag");
        }

        NbtList nbtList = new NbtList();


        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemStack = stacks.get(i);
            if (itemStack.isEmpty()) continue;
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);

            throw new RuntimeException("TODO");
            //itemStack.writeNbt(nbtCompound);

            //nbtList.add(nbtCompound);
        }

        nbt.put("Items", nbtList);
    }

    /**
     * Is this a container of identical items?
     * (If it's not a container, or it's empty, it'll return false)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean containsIdenticalItems(ItemStack stack) {

        List<ItemStack> items = BibItem.extractItems(stack);

        if (items == null) {
            return false;
        }

        ItemStack first = null;
        Item first_item = null;
        boolean is_identical = true;

        for (ItemStack entry : items) {

            if (first == null) {
                first = entry;
                first_item = entry.getItem();
                continue;
            }

            if (first_item != entry.getItem()) {
                is_identical = false;
                break;
            }

            if (!BibItem.areNbtEqual(first, entry)) {
                is_identical = false;
                break;
            }
        }

        return is_identical;
    }

    /**
     * Returns wether the two itemstacks have the same nbt data.
     * @TODO: Everywhere this is called we probably also want to check component data now
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean areNbtEqual(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        NbtCompound left_nbt = BibItem.getCustomNbt(left);
        NbtCompound right_nbt = BibItem.getCustomNbt(right);

        if (left_nbt == null && right_nbt != null) {
            return false;
        }
        return left_nbt == null || left_nbt.equals(right_nbt);
    }

    /**
     * Copy component & nbt data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void assignStackData(ItemStack target, ItemStack source) {

        if (BibItem.hasCustomNbt(source)) {
            BibItem.setCustomNbt(target, BibItem.getCustomNbt(source).copy());
        }

        // @TODO: copy component data
    }
}
