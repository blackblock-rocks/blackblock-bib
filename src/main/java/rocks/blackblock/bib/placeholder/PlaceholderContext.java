package rocks.blackblock.bib.placeholder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Context class used by {@link ItemStackPlaceholder}
 * and {@link BlockPlaceholder}
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public class PlaceholderContext implements BibLog.Argable {

    private Collection<BlockPlaceholderResolver> block_resolvers = null;
    private ServerWorldAccess world = null;
    private ItemStack source = null;
    private boolean allow_default_resolvers = true;
    private BlockPos target_pos = null;
    private BlockState target_state = null;
    private boolean check_target_position = true;
    private boolean use_target_replacement_logic = false;

    /**
     * Create a copy of this context
     * @since    0.2.0
     */
    public PlaceholderContext copy() {
        var copy = new PlaceholderContext();
        copy.block_resolvers = this.block_resolvers;
        copy.world = this.world;
        copy.source = this.source;
        copy.allow_default_resolvers = this.allow_default_resolvers;
        copy.target_pos = this.target_pos;
        copy.target_state = this.target_state;
        copy.check_target_position = this.check_target_position;
        copy.use_target_replacement_logic = this.use_target_replacement_logic;
        return copy;
    }

    /**
     * Set the world this is happening in
     *
     * @since    0.2.0
     */
    public void setWorld(ServerWorldAccess world) {
        this.world = world;
    }

    /**
     * Get the world this is happening in
     *
     * @since    0.2.0
     */
    public ServerWorldAccess getWorld() {
        return this.world;
    }

    /**
     * Set the source stack
     *
     * @since    0.2.0
     */
    public void setSourceStack(ItemStack source) {
        this.source = source;
    }

    /**
     * Get the source stack
     *
     * @since    0.2.0
     */
    public ItemStack getSourceStack() {
        return this.source;
    }

    /**
     * Does the source stack exist?
     *
     * @since    0.2.0
     */
    public boolean hasSourceStack() {
        return this.source != null && !this.source.isEmpty();
    }

    /**
     * Get the block item, if it exists
     *
     * @since    0.2.0
     */
    @Nullable
    public BlockItem getBlockItem() {

        if (this.target_state != null) {
            var item = this.target_state.getBlock().asItem();

            if (item instanceof BlockItem block_item) {
                return block_item;
            }
        }

        if (this.source != null) {
            var item = this.source.getItem();

            if (item instanceof BlockItem block_item) {
                return block_item;
            }
        }

        return null;
    }

    /**
     * Get the current state of the target block
     *
     * @since    0.2.0
     */
    @Nullable
    public BlockState getCurrentState() {

        if (this.world == null) {
            return null;
        }

        if (this.target_pos == null) {
            return null;
        }

        return this.world.getBlockState(this.target_pos);
    }

    /**
     * Get the wanted BlockState of the target block position
     *
     * @since    0.2.0
     */
    public BlockState getWantedTargetState() {
        return this.target_state;
    }

    /**
     * Set the wanted BlockState of the target block position
     *
     * @since    0.2.0
     */
    public void setWantedTargetState(BlockState target_state) {
        this.target_state = target_state;
    }

    /**
     * Should the target position be checked to see if a block can be placed?
     *
     * @since    0.2.0
     */
    public boolean getCheckTargetPosition() {
        return this.check_target_position;
    }

    /**
     * Set whether the target position should be checked to see if a block can be placed
     * @since    0.2.0
     */
    public void setCheckTargetPosition(boolean check_target_position) {
        this.check_target_position = check_target_position;
    }

    /**
     * Should we use target replacement logic?
     * This basically means that the target block doesn't
     * have to be empty to be able to place the block
     * @since    0.2.0
     */
    public boolean getUseTargetReplacementLogic() {
        return this.use_target_replacement_logic;
    }

    /**
     * Set whether we should use target replacement logic
     * @since    0.2.0
     */
    public void setUseTargetReplacementLogic(boolean use_target_replacement_logic) {
        this.use_target_replacement_logic = use_target_replacement_logic;
    }

    /**
     * Should we allow the default resolvers?
     *
     * @since    0.2.0
     */
    public boolean getAllowDefaultResolvers() {
        return this.allow_default_resolvers;
    }

    /**
     * Set whether we should allow the default resolvers
     *
     * @since    0.2.0
     */
    public void setAllowDefaultResolvers(boolean allow_default_resolvers) {
        this.allow_default_resolvers = allow_default_resolvers;
    }

    /**
     * Get all the allowed block resolvers
     *
     * @since    0.2.0
     */
    public Collection<BlockPlaceholderResolver> getBlockResolvers() {

        if (this.block_resolvers == null && this.getAllowDefaultResolvers()) {
            return BlockPlaceholderResolvers.DEFAULT_RESOLVERS;
        }

        Set<BlockPlaceholderResolver> resolvers = new HashSet<>();

        if (this.block_resolvers != null) {
            resolvers.addAll(this.block_resolvers);
        }

        if (this.getAllowDefaultResolvers()) {
            resolvers.addAll(BlockPlaceholderResolvers.DEFAULT_RESOLVERS);
        }

        return resolvers;
    }

    /**
     * Add a block placeholder resolver
     *
     * @since    0.2.0
     */
    public void addBlockPlaceholderResolver(BlockPlaceholderResolver resolver) {

        if (resolver == null) {
            return;
        }

        if (this.block_resolvers == null) {
            this.block_resolvers = new ArrayList<>();
        }

        this.block_resolvers.add(resolver);
    }

    /**
     * Set the target position of the block
     *
     * @since    0.2.0
     */
    public void setTargetPos(BlockPos target_pos) {
        this.target_pos = target_pos;
    }

    /**
     * Get the target position of the block
     *
     * @since    0.2.0
     */
    public BlockPos getTargetPos() {
        return this.target_pos;
    }

    /**
     * Make sur ethe suggested result is valid
     *
     * @since    0.2.0
     */
    private boolean validateSuggestedResult(Result result) {

        BibLog.log("  -- Validating suggested result", result);

        if (result.isEmpty()) {
            return false;
        }

        var copy = this.copy();
        copy.setSourceStack(result.getStack());
        copy.setWantedTargetState(result.getState());

        var can_be_placed = BlockPlaceholderResolver.canBlockBePlaced(copy);

        BibLog.log("    -- Can be placed?", can_be_placed);

        return can_be_placed;
    }

    /**
     * Suggest a Result as a result
     *
     * @since    0.2.0
     */
    public Result suggest(Result result) {

        if (!this.validateSuggestedResult(result)) {
            return Result.EMPTY;
        }

        return Result.of(result);
    }

    /**
     * Suggest an ItemStack as a result
     *
     * @since    0.2.0
     */
    public Result suggest(ItemStack stack) {
        var result = Result.of(stack);

        if (!this.validateSuggestedResult(result)) {
            return Result.EMPTY;
        }

        return result;
    }

    /**
     * Suggest a Block as a result
     *
     * @since    0.2.0
     */
    public Result suggest(Block block) {
        var result = Result.of(block);

        if (!this.validateSuggestedResult(result)) {
            return Result.EMPTY;
        }

        return result;
    }

    /**
     * Get the Arg representation for this instance
     */
    @Override
    public BibLog.Arg toBBLogArg() {
        return BibLog.createArg(this)
                .add("block_resolvers", this.block_resolvers)
                .add("world", this.world)
                .add("source", this.source)
                .add("allow_default_resolvers", this.allow_default_resolvers)
                .add("target_pos", this.target_pos)
                .add("target_state", this.target_state)
                .add("check_target_position", this.check_target_position)
                .add("use_target_replacement_logic", this.use_target_replacement_logic);
    }

    @Override
    public String toString() {
        return this.toBBLogArg().toString();
    }

    /**
     * Result wrapper
     *
     * @since    0.2.0
     */
    public static class Result implements BibLog.Argable {

        public static Result EMPTY = new Result();
        public ItemStack result_stack = null;
        public BlockState result_state = null;

        /**
         * Create a result of the given block
         * @since    0.2.0
         */
        public static Result of(Block block) {
            return of(block.getDefaultState());
        }

        /**
         * Create a result of the given block state
         * @since    0.2.0
         */
        public static Result of(BlockState state) {
            var result = new Result();
            result.result_state = state;
            return result;
        }

        /**
         * Create a result of the given stack
         * @since    0.2.0
         */
        public static Result of(ItemStack stack) {
            var result = new Result();
            result.result_stack = stack;
            return result;
        }

        /**
         * Create a result of the given result
         * @since    0.2.0
         */
        public static Result of(Result result) {
            var result2 = new Result();
            result2.result_stack = result.result_stack;
            result2.result_state = result.result_state;
            return result2;
        }

        /**
         * Did we fail to find a result?
         *
         * @since    0.2.0
         */
        public boolean isEmpty() {
            return this.result_stack == null && this.result_state == null;
        }

        /**
         * Get the stack representation of this result
         *
         * @since    0.2.0
         */
        public ItemStack getStack() {

            if (this.result_stack == null) {
                if (this.result_state != null) {
                    // Depending on the block and its wanted state,
                    // this might not be the correct result!
                    this.result_stack = new ItemStack(this.result_state.getBlock().asItem());
                }
            }

            return this.result_stack;
        }

        /**
         * Get the blockstate representation of this result
         *
         * @since    0.2.0
         */
        public BlockState getState() {
            return this.result_state;
        }

        /**
         * Get the Arg representation for this instance
         */
        @Override
        public BibLog.Arg toBBLogArg() {
            return BibLog.createArg(this)
                    .add("stack", this.getStack())
                    .add("state", this.getState());
        }

        @Override
        public String toString() {
            return this.toBBLogArg().toString();
        }
    }
}
