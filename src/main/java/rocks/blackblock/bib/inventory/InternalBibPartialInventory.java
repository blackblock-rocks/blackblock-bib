package rocks.blackblock.bib.inventory;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.util.BibLog;

import java.util.List;

/**
 * An inventory restricted to certain slots.
 * Those slots will be remapped, starting at 0.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
@ApiStatus.Internal()
public class InternalBibPartialInventory implements Inventory {

    private final List<Integer> slot_index;
    private final Inventory inventory;

    /**
     * Create a new PartialInventory
     *
     * @param   inventory
     * @param   slots
     */
    public InternalBibPartialInventory(Inventory inventory, List<Integer> slots) {
        this.inventory = inventory;
        this.slot_index = slots;
    }

    /**
     * Get the size of this inventory
     * @since    0.1.0
     */
    @Override
    public int size() {
        return this.slot_index.size();
    }

    /**
     * Check if this inventory is empty
     * @since    0.1.0
     */
    @Override
    public boolean isEmpty() {

        for (int slot : this.slot_index) {
            if (!this.inventory.getStack(slot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the stack in the given slot
     * @since    0.1.0
     */
    @Override
    public ItemStack getStack(int slot) {

        // If the slot is out of bounds, return an empty stack
        if (slot < 0 || slot >= this.slot_index.size()) {
            return ItemStack.EMPTY;
        }

        int real_slot = this.slot_index.get(slot);

        return this.inventory.getStack(real_slot);
    }

    /**
     * Remove the stack in the given slot
     * @since    0.1.0
     */
    @Override
    public ItemStack removeStack(int slot, int amount) {

        // If the slot is out of bounds, return an empty stack
        if (slot < 0 || slot >= this.slot_index.size()) {
            return ItemStack.EMPTY;
        }

        int real_slot = this.slot_index.get(slot);

        return this.inventory.removeStack(real_slot, amount);
    }

    /**
     * Remove the stack in the given slot
     * @since    0.1.0
     */
    @Override
    public ItemStack removeStack(int slot) {

        // If the slot is out of bounds, return an empty stack
        if (slot < 0 || slot >= this.slot_index.size()) {
            return ItemStack.EMPTY;
        }

        int real_slot = this.slot_index.get(slot);

        return this.inventory.removeStack(real_slot);
    }

    /**
     * Set the stack in the given slot
     * @since    0.1.0
     */
    @Override
    public void setStack(int slot, ItemStack stack) {

        // If the slot is out of bounds, do nothing
        if (slot < 0 || slot >= this.slot_index.size()) {
            return;
        }

        int real_slot = this.slot_index.get(slot);

        this.inventory.setStack(real_slot, stack);
    }

    /**
     * Get the max stack size in the given slot
     * @since    0.1.0
     */
    @Override
    public int getMaxCountPerStack() {
        return this.inventory.getMaxCountPerStack();
    }

    /**
     * Check if this inventory can be modified
     * @since    0.1.0
     */
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    /**
     * Clear this inventory
     * @since    0.1.0
     */
    @Override
    public void clear() {
        for (int slot : this.slot_index) {
            this.inventory.setStack(slot, ItemStack.EMPTY);
        }
    }

    /**
     * Mark this inventory as dirty
     * @since    0.1.0
     */
    @Override
    public void markDirty() {
        this.inventory.markDirty();
    }

    /**
     * Return a string representation of this instance
     * @since    0.1.0
     */
    @Override
    public String toString() {

        BibLog.Arg arg = BibLog.createArg(this);

        arg.add("inventory", this.inventory);
        arg.add("slots", this.slot_index);

        return arg.toString();
    }
}
