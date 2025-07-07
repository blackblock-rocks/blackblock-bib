package rocks.blackblock.bib.item;

import net.minecraft.block.Block;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import rocks.blackblock.bib.interfaces.BlackblockItem;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.util.BibText;

import java.util.function.Consumer;

/**
 * Internal Base BlockItem class
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.4.1
 */
@SuppressWarnings("unused")
public abstract class InternalBaseBlockItem extends BlockItem implements BlackblockItem {

    public InternalBaseBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    /**
     * Forward tooltips to the custom tooltip builder
     * @since    0.7.1
     */
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        try {
            var builder = new BibText.TooltipBuilder(stack, context, displayComponent, textConsumer, type);
            this.appendTooltip(builder);
            builder.flushLinesToTarget();
        } catch (Exception e) {
            GlitchGuru.registerThrowable(e);
        }
    }

    /**
     * Add tooltips using the custom tooltip builder
     * @since    0.7.1
     */
    @Override
    public void appendTooltip(BibText.TooltipBuilder builder) {

    }
}
