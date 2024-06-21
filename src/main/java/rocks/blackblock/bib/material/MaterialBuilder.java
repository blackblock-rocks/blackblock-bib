package rocks.blackblock.bib.material;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;

/**
 * Builder class for making ArmorMaterial
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class MaterialBuilder<T> {

    // Has it been registered?
    protected boolean has_been_registered = false;

    // The identifier of this material
    protected final String id;

    // The lowest enchantability in use is 9, so default to that
    protected int enchantability = 9;

    // The item used to repair it
    protected Ingredient repair_ingredient = null;

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected MaterialBuilder(String id) {
        this.id = id;
    }

    /**
     * Set the enchantability (for the enchanting table):
     * The higher a material's enchantability,
     * the greater the chances of getting multiple and high-level enchantments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public T setEnchantability(int enchantability) {
        this.enchantability = enchantability;
        return (T) this;
    }

    /**
     * Set the ingredient (item) that can be used to repair the armor
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public T setRepairItem(Item repair_item) {
        this.repair_ingredient = Ingredient.ofItems(repair_item);
        return (T) this;
    }

    /**
     * Set the ingredient (item) that can be used to repair the armor
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public T setRepairItem(Ingredient repair_ingredient) {
        this.repair_ingredient = repair_ingredient;
        return (T) this;
    }
}
