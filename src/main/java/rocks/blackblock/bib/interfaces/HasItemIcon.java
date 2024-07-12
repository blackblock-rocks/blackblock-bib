package rocks.blackblock.bib.interfaces;

import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates the class can be represented with a Minecraft item
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface HasItemIcon {

    /**
     * Get the item icon
     *
     * @since   0.2.0
     */
    @Nullable
    Item getItemIcon();
}
