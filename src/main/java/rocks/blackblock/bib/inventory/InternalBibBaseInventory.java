package rocks.blackblock.bib.inventory;


import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.util.BibInventory;
import rocks.blackblock.bib.util.BibLog;

import java.util.Iterator;
import java.util.List;

/**
 * A basic inventory interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public interface InternalBibBaseInventory extends Inventory, Iterable<ItemStack> {

    /**
     * Method that gets the contents
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    DefaultedList<ItemStack> getContents();

    /**
     * Method that sets the contents
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    void setContents(DefaultedList<ItemStack> contents);

    /**
     * This method should return the BlockItem you want to use in case
     * the inventory data should be written to a dropped item
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default BlockItem getDroppedItem() {
        return null;
    }

    /**
     * Get an ItemStack representation of this block
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default ItemStack getItemStack() {

        BlockItem item = this.getDroppedItem();

        if (item == null) {
            return null;
        }

        ItemStack stack = new ItemStack(item);

        if (this instanceof BlockEntity block_entity) {
            stack.applyComponentsFrom(block_entity.createComponentMap());
        } else {
            BibLog.log("  »» Uhoh, failed to create ItemStack for", this);
        }

        /*
        if (!this.isEmpty()) {
            NbtCompound nbt = this.writeInventoryToNbt(new NbtCompound());

            if (!nbt.isEmpty()) {
                stack.setSubNbt("BlockEntityTag", nbt);
            }
        }

        if (this instanceof Nameable named) {
            if (named.hasCustomName()) {
                stack.setCustomName(named.getCustomName());
            }
        }*/

        return stack;
    }

    /**
     * Is this inventory empty?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default boolean isEmpty() {
        Iterator<ItemStack> var1 = this.getContents().iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    /**
     * Count how many stacks are in this inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default int countStacks() {

        int result = 0;

        for (ItemStack stack : this.getContents()) {
            if (!stack.isEmpty()) {
                result++;
            }
        }

        return result;
    }

    /**
     * Get the ItemStack at the given slot
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default ItemStack getStack(int slot) {
        return this.getContents().get(slot);
    }

    /**
     * Remove a certain amount of ItemStack at the given slot
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack result = this.removeStackSilently(slot, amount);
        this.onStackRemoved(slot, result);
        this.fireContentChangedEvents();
        return result;
    }

    /**
     * Remove the entire ItemStack at the given slot
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default ItemStack removeStack(int slot) {

        ItemStack result = null;

        try {
            result = this.removeStackSilently(slot);
            this.onStackRemoved(slot, result);
            this.fireContentChangedEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Remove a stack silently
     *
     * @since    0.1.0
     */
    default ItemStack removeStackSilently(int slot) {
        return Inventories.removeStack(this.getContents(), slot);
    }

    /**
     * Remove a stack silently
     *
     * @since    0.1.0
     */
    default ItemStack removeStackSilently(int slot, int amount) {
        return Inventories.splitStack(this.getContents(), slot, amount);
    }

    /**
     * Called after an itemstack has been removed from a slot (through a `removeStack` call)
     * but called before contentChangeEvents are fired
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.1
     */
    default void onStackRemoved(int slot, ItemStack removed_stack) {
        // NOOP
    }

    /**
     * Set the ItemStack at the given slot
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default void setStack(int slot, ItemStack stack) {
        this.setStackSilently(slot, stack);
        this.fireContentChangedEvents();
    }

    /**
     * Set the ItemStack at the given slot silently
     *
     * @since    0.1.0
     */
    default void setStackSilently(int slot, ItemStack stack) {
        this.getContents().set(slot, stack);
    }

    /**
     * Set the contents from nbt data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void setContentsFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        DefaultedList<ItemStack> contents = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, contents, registries);
        this.setContents(contents);
    }

    /**
     * Write the contents to nbt data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default NbtCompound writeInventoryToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {

        if (this.getContents() == null) {
            return nbt;
        }

        Inventories.writeNbt(nbt, this.getContents(), registries);
        return nbt;
    }

    /**
     * Clear the entire inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void clear() {
        this.getContents().clear();
        this.fireContentChangedEvents();
    }

    /**
     * Mark the inventory as dirty
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    default void markDirty() {

        if (this.getContents() == null) {
            return;
        }

        this.contentsChanged();
    }

    /**
     * Mark the inventory as dirty
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void fireContentChangedEvents() {

        if (this.getContents() == null) {
            return;
        }

        List<InventoryChangedListener> listeners = this.getListeners();

        if (listeners != null) {
            for (InventoryChangedListener listener : List.copyOf(listeners)) {
                listener.onInventoryChanged(this);
            }
        }

        this.contentsChanged();
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    /**
     * Create the contents iterator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    @Override
    default Iterator<ItemStack> iterator() {
        return this.getContents().iterator();
    }

    /**
     * Get the listeners
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default List<InventoryChangedListener> getListeners() {
        return null;
    }

    /**
     * Set the listeners
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void setListeners(List<InventoryChangedListener> listeners) {}

    /**
     * Add a listener
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void addListener(InventoryChangedListener listener) {

        List<InventoryChangedListener> listeners = this.getListeners();

        if (listeners != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void removeListener(InventoryChangedListener listener) {

        List<InventoryChangedListener> listeners = this.getListeners();

        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Register a player interacting with this inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void openedByPlayer(PlayerEntity player) {
        // NOOP
    }

    /**
     * Unregister a player interacting with this inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    default void closedByPlayer(PlayerEntity player) {
        // NOOP
    }

    void contentsChanged();
}