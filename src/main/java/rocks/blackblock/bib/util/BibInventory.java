package rocks.blackblock.bib.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.inventory.*;

import java.util.List;

/**
 * Library class for working with inventories
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibInventory {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibInventory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * A basic inventory interface with some default functionality
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface Base extends InternalBibBaseInventory {}

    /**
     * A dummy inventory that doesn't do anything
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Dummy extends InternalBibDummyInventory {
        public Dummy(int size) {
            super(size);
        }
    }

    /**
     * An inventory that is always empty and has 0 slots
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Empty extends InternalBibEmptyInventory {
        public static final Empty INSTANCE = new Empty();
    }

    /**
     * Act as an inventory proxy for another inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface Proxy extends InternalBibProxyInventory {}

    /**
     * An inventory restricted to certain slots.
     * Those slots will be remapped, starting at 0.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Partial extends InternalBibPartialInventory {
        public Partial(Inventory inventory, List<Integer> slots) {
            super(inventory, slots);
        }
    }

    /**
     * Get the comparator output of an inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static int calculateComparatorOutput(@Nullable Inventory inventory) {
        return ScreenHandler.calculateComparatorOutput(inventory);
    }
}
