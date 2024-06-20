package rocks.blackblock.bib.augment;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * The PerUUID & PerPlayer components can also implement this interface
 * to be notified when a player's inventory changes.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.1
 */
public interface PlayerInventoryChangeListener extends Augment {

    /**
     * Called when a player's inventory changes
     *
     * @param    player           The player whose inventory changed
     * @param    updated_stack    The updated stack in the inventory
     */
    void onPlayerInventoryChange(PlayerEntity player, ItemStack updated_stack);
}
