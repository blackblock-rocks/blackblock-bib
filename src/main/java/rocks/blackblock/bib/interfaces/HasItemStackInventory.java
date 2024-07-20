package rocks.blackblock.bib.interfaces;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates the class can have an inventory
 * stored in the ItemStack's NBT data
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface HasItemStackInventory {
    /**
     * Get the inventory
     * @since   0.2.0
     */
    @Nullable
    Inventory getItemStackInventory(ItemStack stack);
}
