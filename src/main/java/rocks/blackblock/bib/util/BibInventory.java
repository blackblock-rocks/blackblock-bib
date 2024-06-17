package rocks.blackblock.bib.util;

import rocks.blackblock.bib.inventory.InternalBibBaseInventory;
import rocks.blackblock.bib.inventory.InternalBibDummyInventory;
import rocks.blackblock.bib.inventory.InternalBibEmptyInventory;
import rocks.blackblock.bib.inventory.InternalBibProxyInventory;

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

}
