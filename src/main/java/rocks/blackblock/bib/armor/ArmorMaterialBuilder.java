package rocks.blackblock.bib.armor;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import rocks.blackblock.bib.mixin.ArmorMaterialsAccessor;

import java.util.EnumMap;
import java.util.List;

/**
 * Builder class for making ArmorMaterial
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class ArmorMaterialBuilder {

    // Has it been registered?
    private boolean has_been_registered = false;

    // The eventual registry entry
    private RegistryEntry<ArmorMaterial> registry_entry = null;

    // The identifier of this material
    private final String id;
    private final EnumMap<ArmorItem.Type, Integer> defenses = new EnumMap<>(ArmorItem.Type.class);

    // The lowest enchantability in use is 9, so default to that
    private int enchantability = 9;

    // Use the generic sound by default
    private RegistryEntry<SoundEvent> equip_sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;

    private float toughness = 0.0F;
    private float knockback_resistance = 0.0F;
    private Ingredient repair_ingredient = null;

    // This is for leather only, not implemented here
    private List<ArmorMaterial.Layer> layers = null;

    /**
     * Create a new instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ArmorMaterialBuilder create(String id) {
        return new ArmorMaterialBuilder(id);
    }

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private ArmorMaterialBuilder(String id) {
        this.id = id;
    }

    /**
     * Set the defense value of a specific armor type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setDefense(ArmorItem.Type armor_type, int amount) {
        this.defenses.put(armor_type, amount);
        return this;
    }

    /**
     * Set the enchantability (for the enchanting table):
     * The higher a material's enchantability,
     * the greater the chances of getting multiple and high-level enchantments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setEnchantability(int enchantability) {
        this.enchantability = enchantability;
        return this;
    }

    /**
     * Set the sound to use when a player equips it
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setEquipSound(RegistryEntry<SoundEvent> equip_sound) {
        this.equip_sound = equip_sound;
        return this;
    }

    /**
     * Set the toughness of the material.
     * Normally: the more powerful the attack, the less the armor reduces the amount of received damage.
     * Toughness is like a "second layer" of protection. By default, only diamond & netherite have this.
     * Diamond offers 2.0, netherite 3.0
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setToughness(float toughness) {
        this.toughness = toughness;
        return this;
    }

    /**
     * Set the knockback resistance of the material.
     * This is the percentage (as a float) of how much the knockback effect is reduced.
     * By default, only netherite has this (at 0.1f, so 10%.
     * A full set of netherite armor would reduce it by 40%)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setKnockbackResistance(float resistance) {
        this.knockback_resistance = resistance;
        return this;
    }

    /**
     * Set the ingredient (item) that can be used to repair the armor
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setRepairItem(Item repair_item) {
        this.repair_ingredient = Ingredient.ofItems(repair_item);
        return this;
    }

    /**
     * Set the ingredient (item) that can be used to repair the armor
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setRepairItem(Ingredient repair_ingredient) {
        this.repair_ingredient = repair_ingredient;
        return this;
    }

    /**
     * Actually register the material
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public RegistryEntry<ArmorMaterial> register() {
        if (this.has_been_registered) {
            throw new RuntimeException("Trying to register armor material '" + this.id + "' again!");
        }

        this.has_been_registered = true;

        // If there is no repair ingredient, use something expensive
        if (this.repair_ingredient == null) {
            this.setRepairItem(Items.DIAMOND);
        }

        this.registry_entry = ArmorMaterialsAccessor.register(
                this.id,
                this.defenses,
                this.enchantability,
                this.equip_sound,
                this.toughness,
                this.knockback_resistance,
                () -> this.repair_ingredient
        );

        return this.registry_entry;
    }

    /**
     * Get the registry entry (if available already)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public RegistryEntry<ArmorMaterial> getRegistryEntry() {
        return this.registry_entry;
    }
}
