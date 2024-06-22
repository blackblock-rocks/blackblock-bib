package rocks.blackblock.bib.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.util.BibBlock;

/**
 * Wrapper class for handling block interactions
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class InternalBibBlockInteraction extends BibBlock.BaseInteraction {

    // The interaction result
    protected BibBlock.InteractionResult result = null;

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public InternalBibBlockInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        super(state, world, pos, player, hit);
    }

    /**
     * Set the interaction result
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public void setInteractionResult(BibBlock.InteractionResult result) {
        this.result = result;
    }

    /**
     * Get the eventual interaction result
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public BibBlock.InteractionResult getInteractionResult() {

        if (this.result == null) {
            return BibBlock.InteractionResult.PASS;
        }

        return this.result;
    }
}
