package rocks.blackblock.bib.armor;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.material.MaterialBuilder;

import java.util.EnumMap;

/**
 * Builder class for making ArmorMaterial
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class ArmorMaterialBuilder extends MaterialBuilder<ArmorMaterialBuilder> {

    // The eventual registry entry
    private RegistryEntry<ArmorMaterial> registry_entry = null;

    private final EnumMap<EquipmentType, Integer> defenses = new EnumMap<>(EquipmentType.class);

    // Use the generic sound by default
    private RegistryEntry<SoundEvent> equip_sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;

    private float toughness = 0.0F;
    private float knockback_resistance = 0.0F;

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
        super(id);
    }

    /**
     * Set the defense value of a specific armor type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ArmorMaterialBuilder setDefense(EquipmentType armor_type, int amount) {
        this.defenses.put(armor_type, amount);
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
        if (this.repair_item == null) {
            this.setRepairItem(ItemTags.DIAMOND_TOOL_MATERIALS);
        }

        Identifier armor_id = BibMod.id(this.id);

        // @TODO: Implement new ArmorMaterial registration
        RegistryKey<EquipmentAsset> equipmentAsset = null;

        /*
        ArmorMaterial material = new ArmorMaterial(
                this.defenses,
                this.enchantability,
                this.equip_sound,
                () -> this.repair_ingredient,
                material_list,
                this.toughness,
                this.knockback_resistance
        );

        this.registry_entry = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
                armor_id,
                material
        );
        */

        if (this.registry_entry == null) {
            return RegistryEntry.of(ArmorMaterials.GOLD);
        }

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
