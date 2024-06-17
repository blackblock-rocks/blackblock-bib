package rocks.blackblock.bib.inventory;

import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import rocks.blackblock.bib.util.BibInventory;

import java.util.List;

/**
 * A dummy inventory that doesn't do anything
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class InternalBibDummyInventory implements BibInventory.Base {

    public final static InternalBibDummyInventory EMPTY = new InternalBibDummyInventory(0);

    private final int size;
    private DefaultedList<ItemStack> contents;
    private List<InventoryChangedListener> listeners;

    public InternalBibDummyInventory(int size) {
        this.size = size;
        this.setContents(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    @Override
    public DefaultedList<ItemStack> getContents() {
        return this.contents;
    }

    @Override
    public void setContents(DefaultedList<ItemStack> contents) {
        this.contents = contents;
    }

    @Override
    public void contentsChanged() {

    }

    @Override
    public int size() {
        return this.size;
    }
}
