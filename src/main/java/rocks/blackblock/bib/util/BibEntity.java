package rocks.blackblock.bib.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;

/**
 * Library class for working with entities
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibEntity {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibEntity() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Is this entity only holding garbage?
     * Holding nothing will also return true
     * @since    0.2.0
     */
    public static boolean isOnlyHoldingGarbage(LivingEntity entity) {

        if (entity == null || !entity.isAlive()) {
            return true;
        }

        for (var slot : EquipmentSlot.values()) {

            ItemStack stack = entity.getEquippedStack(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (BibItem.isGarbage(stack)) {
                continue;
            }

            return false;
        }

        return true;
    }
}
