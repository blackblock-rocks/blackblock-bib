package rocks.blackblock.bib.tool;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import rocks.blackblock.bib.material.MaterialBuilder;

/**
 * Builder class for making ToolMaterials
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class ToolMaterialBuilder extends MaterialBuilder<ToolMaterialBuilder> {

    // The "mining level": what the tool can mine
    private TagKey<Block> inverse_tag = BlockTags.INCORRECT_FOR_STONE_TOOL;

    // The lowest durability (uses) is currently gold (32)
    private int durability = 32;

    // The lowest mining speed is currently wood (2.0f)
    private float mining_speed = 2.0f;

    // The lowest attack damage is currently wood (0.0f)
    private float attack_damage = 0.0f;

    // The lowest enchantability is currently stone (5)
    protected int enchantability = 5;

    // The eventual created material
    protected CustomToolMaterial custom_material = null;

    /**
     * Create a new instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static ToolMaterialBuilder create(String id) {
        return new ToolMaterialBuilder(id);
    }

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private ToolMaterialBuilder(String id) {
        super(id);
    }

    /**
     * Set the mining level
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterialBuilder setMiningLevel(int level) {
        // Parse the old-style mining level integers
        // Note: gold was never an option, it got lumped in with wood
        this.inverse_tag = switch (level) {
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            case 4 -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
            default -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
        };

        return this;
    }

    /**
     * Set the mining level as an inverse tag (the blocks it CAN'T mine)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterialBuilder setMiningLevel(TagKey<Block> inverse_tag) {
        this.inverse_tag = inverse_tag;
        return this;
    }

    /**
     * Set the durability (uses) value
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterialBuilder setDurability(int durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Set the mining speed
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterialBuilder setMiningSpeed(float mining_speed) {
        this.mining_speed = mining_speed;
        return this;
    }

    /**
     * Set the attack damage
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterialBuilder setAttackDamage(float attack_damage) {
        this.attack_damage = attack_damage;
        return this;
    }

    /**
     * Actually register the material
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public ToolMaterial register() {

        if (this.has_been_registered) {
            throw new RuntimeException("Trying to register tool material '" + this.id + "' again!");
        }

        this.has_been_registered = true;

        // If there is no repair ingredient, use something expensive
        if (this.repair_item == null) {
            this.setRepairItem(ItemTags.DIAMOND_TOOL_MATERIALS);
        }

        this.custom_material = new CustomToolMaterial(
                this.inverse_tag,
                this.durability,
                this.mining_speed,
                this.attack_damage,
                this.enchantability,
                this.repair_item
        );

        return this.custom_material;
    }
}
