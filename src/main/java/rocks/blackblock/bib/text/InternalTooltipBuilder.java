package rocks.blackblock.bib.text;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.util.BibLog;

import java.util.function.Consumer;

/**
 * Wrapper class for creating tooltips
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.4.1
 */
@SuppressWarnings("unused")
@ApiStatus.Internal
public abstract class InternalTooltipBuilder<T extends InternalLore<?>> extends InternalLore<T> implements BibLog.Argable {

    protected ItemStack stack = null;
    protected Item.TooltipContext context = null;
    protected TooltipDisplayComponent displayComponent = null;
    protected Consumer<Text> textConsumer = null;
    protected TooltipType type = null;

    /**
     * Initialize the builder
     * @since    0.4.1
     */
    public InternalTooltipBuilder(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        this.stack = stack;
        this.context = context;
        this.displayComponent = displayComponent;
        this.textConsumer = textConsumer;
        this.type = type;
    }

    /**
     * Get the stack
     * @since    0.4.1
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Get the context
     * @since    0.4.1
     */
    public Item.TooltipContext getContext() {
        return this.context;
    }

    /**
     * Get the display component
     * @since    0.4.1
     */
    public TooltipDisplayComponent getDisplayComponent() {
        return this.displayComponent;
    }

    /**
     * Get the tooltip type
     * @since    0.4.1
     */
    public TooltipType getType() {
        return this.type;
    }

    /**
     * Flush all lines to the actual tooltip
     * @since   0.4.1
     */
    public void flushLinesToTarget() {

        if (this.textConsumer == null) {
            return;
        }

        for (Text line : this.lines) {
            this.textConsumer.accept(line);
        }
    }

    /**
     * Does this have an item stack?
     * @since   0.4.1
     */
    public boolean hasItemStack() {
        return this.stack != null && !this.stack.isEmpty();
    }

    /**
     * Create an Arg representation of this instance
     * @since   0.4.1
     */
    @Override
    public BibLog.Arg toBBLogArg() {
        return BibLog.createArg(this)
                .add("stack", this.stack)
                .add("context", this.context)
                .add("display_component", this.displayComponent)
                .add("text_consumer", this.textConsumer)
                .add("type", this.type);
    }

    /**
     * Create a string representation of this instance
     * @since   0.4.1
     */
    @Override
    public String toString() {
        return this.toBBLogArg().toString();
    }
}
