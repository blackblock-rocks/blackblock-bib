package rocks.blackblock.bib.tool;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;

/**
 * Base Custom Tool Material class
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class CustomToolMaterial extends ToolMaterial {
    public CustomToolMaterial(TagKey<Block> inverseTag, int itemDurability, float miningSpeed, float attackDamage, int enchantability, TagKey<Item> repair_item) {
        super(inverseTag, itemDurability, miningSpeed, attackDamage, enchantability, repair_item);
    }
}