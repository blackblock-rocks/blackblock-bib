package rocks.blackblock.bib.interaction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibBlock;
import rocks.blackblock.bib.util.BibLog;

/**
 * Wrapper class for handling interactions
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
@ApiStatus.Internal
public abstract class InternalBibBlockBaseInteraction {

    // The blockstate being interacted with
    protected BlockState state;

    // The world where the interaction is taking place
    protected World world;

    // The position of the block
    protected BlockPos pos;

    // The player doing the interaction
    protected PlayerEntity player;

    // The original BlockHitResult
    protected BlockHitResult block_hit;

    /**
     * Keep the constructor private
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public InternalBibBlockBaseInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        this.state = state;
        this.world = world;
        this.pos = pos;
        this.player = player;
        this.block_hit = hit;
    }

    /**
     * Get the BlockState being interacted with
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public BlockState getBlockState() {
        return this.state;
    }

    /**
     * Get the world where the interaction is taking place
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public World getWorld() {
        return this.world;
    }

    /**
     * Get the position of the block
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public BlockPos getBlockPos() {
        return this.pos;
    }

    /**
     * Get the player doing the interaction
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public PlayerEntity getPlayer() {
        return this.player;
    }

    /**
     * Get the side of the block being interacted with
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @NotNull
    public Direction getBlockSide() {
        return this.block_hit.getSide();
    }

    /**
     * Try to get a BlockEntity at the position
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public BlockEntity getBlockEntity() {
        return this.world.getBlockEntity(this.pos);
    }

    /**
     * Get a BlockEntity of the expected type at the current position
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public <T extends BlockEntity> T getBlockEntity(BlockEntityType<T> type) {
        return this.world.getBlockEntity(this.pos, type).orElse(null);
    }

    /**
     * Try to open any attached screen.
     * The interaction result will not be changed.
     *
     * This will also check permissions, if there are any.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean attemptToOpenScreen() {

        if (!this.isAllowedToOpenScreen()) {
            return false;
        }

        BlockEntity be_entity = this.getBlockEntity();
        NamedScreenHandlerFactory screen_factory = null;

        if (be_entity instanceof NamedScreenHandlerFactory _screen_factory) {
            screen_factory = _screen_factory;
        } else if (be_entity != null) {
            screen_factory = this.state.createScreenHandlerFactory(this.world, this.pos);
        }

        if (screen_factory == null) {
            return false;
        }

        this.player.openHandledScreen(screen_factory);

        // See if we have to increment any statistic
        Identifier stat_id = null;
        if (be_entity instanceof BibBlock.HasInteractionStatistic has_interaction_statistic) {
            stat_id = has_interaction_statistic.getInteractionStatisticIdentifier();
        } else if (screen_factory != be_entity && screen_factory instanceof BibBlock.HasInteractionStatistic has_interaction_statistic) {
            stat_id = has_interaction_statistic.getInteractionStatisticIdentifier();
        }

        if (stat_id != null) {
            this.player.incrementStat(stat_id);
        }

        return true;
    }

    /**
     * Is the current player allowed to open the attached screen?
     * This might return true, even if there is no screen.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean isAllowedToOpenScreen() {

        Block block = this.getBlockState().getBlock();

        if (block instanceof BibBlock.PlayerGuiPermissionCheck check_permission) {
            if (!check_permission.guiCanBeOpenedBy(this.getSelfAsBaseInteraction())) {
                return false;
            }
        }

        BlockEntity be_entity = this.getBlockEntity();

        if (be_entity == null) {
            return true;
        }

        if (be_entity instanceof BibBlock.PlayerGuiPermissionCheck check_permission) {
            return check_permission.guiCanBeOpenedBy(this.getSelfAsBaseInteraction());
        }

        return true;
    }

    /**
     * Create BibLog.Arg instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public BibLog.Arg createBibLogArg() {

        BibLog.Arg arg = BibLog.createArg(this);

        arg.add("block_state", this.state);
        arg.add("world", this.world);
        arg.add("pos", this.pos);
        arg.add("player", this.player);
        arg.add("hit", this.block_hit);

        return arg;
    }

    /**
     * Return a string representation of this instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public String toString() {
        return this.createBibLogArg().toString();
    }

    /**
     * Get ourselves as a BibBlock.BaseInteraction class
     * Basically just for the compiler
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    protected abstract BibBlock.BaseInteraction getSelfAsBaseInteraction();

    /**
     * Get the original BlockHitResult
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    public BlockHitResult getBlockHitResult() {
        return this.block_hit;
    }
}
