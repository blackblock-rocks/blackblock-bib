package rocks.blackblock.bib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.*;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.collection.CompareForScenario;
import rocks.blackblock.bib.interfaces.HasItemStackInventory;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.item.ItemStack.ITEM_CODEC;

/**
 * Library class for working with Items & ItemStacks
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibItem {

    private static final String UNWRAPPED_ITEM_KEY = "BB:BackupItem";
    private static final Codec<ItemStack> ITEM_DATA_COPY_CODEC = RecordCodecBuilder.create((instance) -> instance.group(ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry), ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemStack::getComponentChanges)).apply(instance, (id, components) -> new ItemStack(id, 1, components)));
    private static final MapCodec<Optional<ItemStack>> ORIGINAL_ITEM_CODEC = ITEM_DATA_COPY_CODEC.optionalFieldOf(UNWRAPPED_ITEM_KEY);

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
     * Return a string representation of the custom data of a stack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String toDebugString(ItemStack stack) {
        return BibLog.createArg(stack).toString();
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
     * Get the custom NBT data if it exists
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static NbtCompound getCustomNbt(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (data == null) {
            return null;
        }

        return data.getNbt();
    }

    /**
     * Get or create the custom NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public static NbtCompound getOrCreateCustomNbt(ItemStack stack) {
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
    public static void setCustomNbt(ItemStack stack, @Nullable NbtCompound nbt) {

        if (nbt == null) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
            return;
        }

        NbtComponent data = NbtComponent.of(nbt);
        stack.set(DataComponentTypes.CUSTOM_DATA, data);
    }

    /**
     * Get a custom sub-nbt value
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static NbtCompound getCustomSubNbt(ItemStack stack, String key) {

        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (data == null) {
            return null;
        }

        NbtCompound nbt = data.getNbt();

        if (nbt == null) {
            return null;
        }

        if (!nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        return nbt.getCompound(key);
    }

    /**
     * Get or create a custom sub-nbt value
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public static NbtCompound getOrCreateCustomSubNbt(ItemStack stack, String key) {

        NbtCompound nbt = getOrCreateCustomNbt(stack);

        if (!nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
            NbtCompound nbtCompound = new NbtCompound();
            nbt.put(key, nbtCompound);
            return nbtCompound;
        }

        return nbt.getCompound(key);
    }

    /**
     * Set a custom sub-nbt value
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setCustomSubNbt(ItemStack stack, String key, NbtElement element) {
        getOrCreateCustomNbt(stack).put(key, element);
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
    public static ItemStack setCustomModelDataValue(ItemStack stack, Integer custom_model_data_value) {

        if (custom_model_data_value == null) {
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        } else {
            var cmd_component = new CustomModelDataComponent(custom_model_data_value);
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, cmd_component);
        }

        return stack;
    }

    /**
     * Set the custom name of the given stack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setCustomName(ItemStack stack, Text text) {

        if (text == null) {
            stack.remove(DataComponentTypes.CUSTOM_NAME);
        } else {
            stack.set(DataComponentTypes.CUSTOM_NAME, text);
        }
    }

    /**
     * Does the given stack have a custom name?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasCustomName(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Text custom_name_component = stack.get(DataComponentTypes.CUSTOM_NAME);

        return custom_name_component != null;
    }

    /**
     * Append a lore instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.2.0
     */
    public static void appendLore(ItemStack stack, BibText.Lore lore) {
        for (Text line : lore.getLines()) {
            BibItem.appendLore(stack, line);
        }
    }

    /**
     * Append the given text to the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void appendLore(ItemStack stack, Text text) {

        if (text == null) {
            return;
        }

        var lore_component = stack.get(DataComponentTypes.LORE);

        if (lore_component == null) {
            lore_component = new LoreComponent(List.of(text));
        } else {
            lore_component = lore_component.with(text);
        }

        stack.set(DataComponentTypes.LORE, lore_component);
    }

    /**
     * Replace the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.2.0
     */
    public static void replaceLore(ItemStack stack, BibText.Lore lore) {
        BibItem.replaceLore(stack, lore.getLines());
    }

    /**
     * Replace the ItemStack's lore
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void replaceLore(ItemStack stack, List<? extends Text> lines) {

        if (lines == null || lines.isEmpty()) {
            stack.remove(DataComponentTypes.LORE);
            return;
        }

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

        if (text == null) {
            stack.remove(DataComponentTypes.LORE);
        } else {
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(text)));
        }
    }

    /**
     * Can the 2 stacks be combined according to Minecraft logic?
     * This might return false for ItemStacks that appear exactly the same.
     * For a more intuitive result, use {@link BibItem#areEqual}
     *
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean canCombine(ItemStack left, ItemStack right) {
        return ItemStack.areItemsAndComponentsEqual(left, right);
    }

    /**
     * Can the 2 given ItemStacks be considered "equal" intuitively?
     * This will ignore empty CustomNBT data, for example.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean areEqual(ItemStack left, ItemStack right) {

        if (left == null || right == null) {
            return false;
        }

        if (!left.isOf(right.getItem())) {
            return false;
        }

        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }

        var left_components = left.getComponents();
        var right_components = right.getComponents();

        if (left_components == right_components) {
            return true;
        }

        if (left_components.equals(right_components)) {
            return true;
        }

        var all_types = BibData.combineSets(left_components.getTypes(), right_components.getTypes());
        var result = true;

        for (ComponentType<?> type : all_types) {
            var left_value = left_components.get(type);
            var right_value = right_components.get(type);

            if (Objects.equals(left_value, right_value)) {
                continue;
            }

            if (type == DataComponentTypes.CUSTOM_DATA) {
                boolean has_left_data = true;
                boolean has_right_data = true;

                if (left_value == null || ((NbtComponent) left_value).isEmpty()) {
                    has_left_data = false;
                }

                if (right_value == null || ((NbtComponent) right_value).isEmpty()) {
                    has_right_data = false;
                }

                // Don't make the result false if both NbtComponents are null or empty
                if (!has_left_data && !has_right_data) {
                    continue;
                }
            }

            result = false;
            break;
        }

        return result;
    }

    /**
     * See if these 2 stacks are equal for the given scenario
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean areEqualForScenario(ItemStack leftStack, ItemStack rightStack, String scenario) {

        Item left = leftStack.getItem();
        Item right = rightStack.getItem();

        // Check if the items are the same
        if (left != right) {
            return false;
        }

        Boolean result = null;

        if (left instanceof CompareForScenario scenarioItem && scenarioItem.supportsScenario(scenario)) {
            result = scenarioItem.compareForScenario(leftStack, rightStack, scenario);

            if (result != null) {
                return result;
            }
        }

        return areEqual(leftStack, rightStack);
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

        return areEqual(left, right);
    }

    /**
     * Serialize an ItemStack into an NbtElement
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null")
    public static NbtElement serializeStack(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
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
    @NotNull
    @Deprecated
    public static List<ItemStack> extractItems(ItemStack stack) {
        return extractInventoryItems(stack);
    }

    /**
     * Write the list of items to the itemstack nbt
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void insertItems(ItemStack stack, List<ItemStack> stacks) {

        NbtCompound nbt = BibItem.getOrCreateCustomNbt(stack);

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

        List<ItemStack> items = BibItem.extractInventoryItems(stack);

        if (items.isEmpty()) {
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

        var source_nbt = BibItem.getCustomNbt(source);

        if (source_nbt != null) {
            BibItem.setCustomNbt(target, source_nbt.copy());
        }

        // @TODO: copy component data
        BibLog.todo("Copy component data when assigning stack data?");
    }

    /**
     * Apply (durability) damage to the given stack held by the given entity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void damageStack(ItemStack stack, int damage_amount, LivingEntity entity) {

        if (entity == null) {
            return;
        }

        stack.damage(damage_amount, entity, LivingEntity.getSlotForHand(entity.getActiveHand()));
    }

    /**
     * Create an "overlayed" ItemStack:
     * This creates a copy of the stack *and* puts all of the
     * original information in the custom NBT,
     * so it can be restored later.
     *
     * Do note: the count is not backed up.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack createOverlay(ItemStack stack) {
        return createOverlay(stack, UNWRAPPED_ITEM_KEY, ORIGINAL_ITEM_CODEC);
    }

    /**
     * Create an "overlayed" ItemStack:
     * This creates a copy of the stack *and* puts all of the
     * original information in the custom NBT,
     * so it can be restored later.
     *
     * Do note: the count is not backed up.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack createOverlay(ItemStack stack, String overlay_name) {
        return createOverlay(stack, overlay_name, ITEM_DATA_COPY_CODEC.optionalFieldOf(overlay_name));
    }

    /**
     * Create an "overlayed" ItemStack:
     * This creates a copy of the stack *and* puts all of the
     * original information in the custom NBT,
     * so it can be restored later.
     *
     * Do note: the count is not backed up.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static ItemStack createOverlay(ItemStack original_stack, String overlay_key, MapCodec<Optional<ItemStack>> overlay_codec) {

        if (original_stack == null) {
            return null;
        }

        if (original_stack == ItemStack.EMPTY) {
            return original_stack;
        }

        NbtCompound original_nbt = BibItem.getCustomNbt(original_stack);

        // See if it is already wrapped.
        // If it is, don't wrap it again!
        if (original_nbt != null && original_nbt.contains(overlay_key)) {
            return original_stack.copy();
        }

        // Create a new stack that will be wrapped
        ItemStack wrapped_stack = original_stack.copy();

        // The NBT encoder requires a registry since 1.21
        RegistryOps<NbtElement> registry_ops = BibMod.getDynamicRegistry().getOps(NbtOps.INSTANCE);

        // Put the entire original item into the custom NBT data
        NbtComponent.DEFAULT.with(registry_ops, overlay_codec, Optional.of(original_stack)).result().ifPresent((nbt) -> {
            wrapped_stack.set(DataComponentTypes.CUSTOM_DATA, nbt);
        });

        // Get the wrapped NBT we just created
        NbtCompound wrapped_nbt = BibItem.getOrCreateCustomNbt(wrapped_stack);

        // Copy over all the other custom NBT data
        BibData.assignDefaults(wrapped_nbt, original_nbt);

        // Set it as the new custom NBT again
        BibItem.setCustomNbt(wrapped_stack, wrapped_nbt);

        return wrapped_stack;
    }

    /**
     * Return the original stack, without the overlay.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack withoutOverlay(ItemStack wrapped_stack) {
        return withoutOverlay(wrapped_stack, UNWRAPPED_ITEM_KEY, ORIGINAL_ITEM_CODEC);
    }

    /**
     * Return the original stack, without the overlay.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ItemStack withoutOverlay(ItemStack wrapped_stack, String overlay_name) {
        return withoutOverlay(wrapped_stack, overlay_name, ITEM_DATA_COPY_CODEC.optionalFieldOf(overlay_name));
    }

    /**
     * Return the original stack, without the overlay.
     * If there is no overlay, a copy of the stack is returned.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static ItemStack withoutOverlay(ItemStack wrapped_stack, String overlay_key, MapCodec<Optional<ItemStack>> overlay_codec) {

        ItemStack unwrapped = null;

        NbtComponent custom_data = wrapped_stack.get(DataComponentTypes.CUSTOM_DATA);

        if (custom_data == null || custom_data.isEmpty()) {
            return wrapped_stack.copy();
        }

        DataResult<Optional<ItemStack>> unwrap_result = custom_data.get(overlay_codec);

        if (unwrap_result.error().isPresent()) {
            DataResult.Error<?> error = unwrap_result.error().get();

            GlitchGuru.registerThrowable(error);

            // Split the error message up into multiple lines:
            // this prevents the tooltip lore from spanning the entire screen
            String message = BibText.createWrappedString(error.message(), 45);
            BibText.Lore error_lore = BibText.createLore(message);

            unwrapped = new ItemStack(Items.ROTTEN_FLESH);
            BibItem.setCustomName(unwrapped, Text.literal("Unwrap Error Item").formatted(Formatting.ITALIC));
            BibItem.appendLore(unwrapped, error_lore.formatted(Formatting.RED));
        } else {
            // Return the original only if it's present
            unwrapped = unwrap_result.getOrThrow().orElse(null);
        }

        if (unwrapped == null) {
            unwrapped = wrapped_stack.copy();
        } else {
            unwrapped.setCount(wrapped_stack.getCount());
        }

        return unwrapped;
    }

    /**
     * Extract items from the given ItemStack-with-inventory
     *
     * @since    0.2.0
     */
    @NotNull
    public static List<ItemStack> extractInventoryItems(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return List.of();
        }

        Item item = stack.getItem();

        if (item instanceof HasItemStackInventory item_with_inventory) {
            Inventory inventory_instance = item_with_inventory.getItemStackInventory(stack);
            return BibInventory.getNonEmptyItems(inventory_instance);
        }

        NbtCompound entity_tag = BibBlock.getBlockEntityData(stack);

        if (entity_tag == null) {
            return List.of();
        }

        if (entity_tag.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList list = entity_tag.getList("Items", NbtElement.COMPOUND_TYPE);

            if (list.isEmpty()) {
                return List.of();
            }

            List<ItemStack> item_contents = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                NbtCompound item_tag = list.getCompound(i);
                ItemStack item_stack = ItemStack.fromNbt(BibMod.getDynamicRegistry(), item_tag).orElse(null);

                BibLog.log(" -- Converted", i, item_tag, "into", item_stack);

                if (item_stack == null || item_stack.isEmpty()) {
                    continue;
                }

                item_contents.add(item_stack);
            }

            return item_contents;
        }

        return List.of();
    }

    /**
     * Can we consider the given stack to be garbage?
     *
     * @since    0.2.0
     */
    public static boolean isGarbage(ItemStack stack) {

        if (stack == null) {
            return true;
        }

        if (stack.isEmpty()) {
            return true;
        }

        Rarity rarity = stack.getRarity();

        if (rarity == Rarity.COMMON) {
            return true;
        }

        Item item = stack.getItem();

        // We don't care about any tool lower or equal to gold,
        // even if it's enchanted
        if (item instanceof ToolItem tool_item) {
            var material = tool_item.getMaterial();

            if (material == ToolMaterials.WOOD) {
                return true;
            }

            if (material == ToolMaterials.STONE) {
                return true;
            }

            if (material == ToolMaterials.IRON) {
                return true;
            }

            if (material == ToolMaterials.GOLD) {
                return true;
            }
        }

        if (item instanceof ArmorItem armor_item) {
            var material = armor_item.getMaterial();

            if (material == ArmorMaterials.LEATHER) {
                return true;
            }

            if (material == ArmorMaterials.CHAIN) {
                return true;
            }

            if (material == ArmorMaterials.IRON) {
                return true;
            }

            if (material == ArmorMaterials.GOLD) {
                return true;
            }

            if (material == ArmorMaterials.TURTLE) {
                return true;
            }
        }

        return false;
    }
}
