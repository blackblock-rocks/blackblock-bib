package rocks.blackblock.bib.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.util.BibBlock;
import rocks.blackblock.bib.util.BibLog;

/**
 * Wrapper class for handling block interactions while carying an item
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class InternalBibBlockWithItemInteraction extends BibBlock.BaseInteraction {

    // The ItemStack being used
    protected ItemStack stack;

    // The hand being used
    protected Hand hand;

    // The result
    protected BibBlock.InteractionWithItemResult result = null;

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public InternalBibBlockWithItemInteraction(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super(state, world, pos, player, hit);
        this.stack = stack;
        this.hand = hand;
    }

    /**
     * Get the ItemStack being used
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public ItemStack getItemStack() {
        return this.stack;
    }

    /**
     * Get the original Hand
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    public Hand getHand() {
        return this.hand;
    }

    /**
     * Set the interaction result
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public void setInteractionResult(BibBlock.InteractionWithItemResult result) {
        this.result = result;
    }

    /**
     * Get the eventual interaction result
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public BibBlock.InteractionWithItemResult getInteractionResult() {

        if (this.result == null) {
            return BibBlock.InteractionWithItemResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return this.result;
    }

    /**
     * Create BibLog.Arg instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public BibLog.Arg createBibLogArg() {
        BibLog.Arg result = super.createBibLogArg();
        result.add("stack", this.stack);
        result.add("hand", this.hand);
        return result;
    }
}
