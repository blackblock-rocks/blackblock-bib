package rocks.blackblock.bib.placeholder;

import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.util.BibItem;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Helps resolve an ItemStack that isn't an
 * Item that has the BlockPlaceholder interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BlockPlaceholderResolver {

    private final String id;
    private Function<PlaceholderContext, PlaceholderContext.Result> resolver = null;

    /**
     * Create a new resolver
     *
     * @since    0.2.0
     */
    public BlockPlaceholderResolver(String id) {
        this.id = id;
    }

    /**
     * Set the resolver function
     *
     * @since    0.2.0
     */
    public BlockPlaceholderResolver setResolver(Function<PlaceholderContext, PlaceholderContext.Result> resolver) {
        this.resolver = resolver;
        return this;
    }

    /**
     * Get the identifier of this resolver
     *
     * @since    0.2.0
     */
    public String getId() {
        return this.id;
    }

    /**
     * Resolve a block from the given stack
     *
     * @since    0.2.0
     */
    public PlaceholderContext.Result apply(PlaceholderContext context) {
        var result = this.resolver.apply(context);

        if (result == null) {
            return PlaceholderContext.Result.EMPTY;
        }

        return result;
    }

    /**
     * Get a block from the given stack
     *
     * @since    0.2.0
     */
    public static PlaceholderContext.Result resolveBlockFromItem(ItemStack stack) {
        PlaceholderContext context = new PlaceholderContext();
        context.setSourceStack(stack);
        return resolveBlockFromItem(context);
    }

    /**
     * Get a block from the given stack.
     * If the context requires target validation, the result might be empty
     * if it can't be placed.
     *
     * @since    0.2.0
     */
    @NotNull
    public static PlaceholderContext.Result resolveBlockFromItem(PlaceholderContext context) {

        if (!context.hasSourceStack()) {
            return PlaceholderContext.Result.EMPTY;
        }

        ItemStack source = context.getSourceStack();
        Item item = source.getItem();

        // Always prefer the block placeholder interface
        if (item instanceof BlockPlaceholder block_placeholder_item) {
            var result = block_placeholder_item.getBlockPlaceholderReplacementStack(context);

            if (result == null) {
                return PlaceholderContext.Result.EMPTY;
            }

            return context.suggest(result);
        }

        // See if we can resolve it with the default resolvers
        for (BlockPlaceholderResolver resolver : context.getBlockResolvers()) {
            PlaceholderContext.Result result = resolver.apply(context);

            if (!result.isEmpty()) {
                return context.suggest(result);
            }
        }

        // If the item is a BlockItem, suggest the source block
        // Don't just suggest the block_item.getBlock() because
        // it might be a BlockEntity that needs data to be valid
        if (item instanceof BlockItem block_item) {
            return context.suggest(source);
        }

        return PlaceholderContext.Result.EMPTY;
    }

    /**
     * Get all the block ItemStacks that can fit the current placeholder context
     * @since    0.2.0
     */
    public static List<ItemStack> filterAllBlockItemStacks(ItemStack stack_with_inventory, PlaceholderContext context) {

        List<ItemStack> result = BibItem.extractInventoryItems(stack_with_inventory);

        if (result.isEmpty()) {
            return result;
        }

        return filterAllBlockItemStacks(result, context);
    }

    /**
     * Get all the block ItemStacks that can fit the current placeholder context
     * @since    0.2.0
     */
    public static List<ItemStack> filterAllBlockItemStacks(List<ItemStack> block_item_stacks, PlaceholderContext context) {

        if (block_item_stacks == null || block_item_stacks.isEmpty()) {
            return List.of();
        }

        List<ItemStack> result = new ArrayList<>(block_item_stacks.size());

        for (ItemStack stack : block_item_stacks) {
            PlaceholderContext copy = context.copy();

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            copy.setSourceStack(stack);

            var resolved = resolveBlockFromItem(copy);

            if (resolved.isEmpty()) {
                continue;
            }

            result.add(resolved.getStack());
        }

        return result;
    }

    /**
     * Check if the given block can be placed at the given position
     * using vanilla logic (no target replacement)
     *
     * @since    0.2.0
     */
    public static boolean canBlockBePlaced(ServerWorldAccess world, BlockPos pos, BlockState state) {
        PlaceholderContext context = new PlaceholderContext();
        context.setWorld(world);
        context.setTargetPos(pos);
        context.setWantedTargetState(state);
        context.setCheckTargetPosition(true);
        context.setUseTargetReplacementLogic(false);
        return canBlockBePlaced(context);
    }

    /**
     * Check if the given block can be placed at the given position
     *
     * @since    0.2.0
     */
    public static boolean canBlockBePlaced(PlaceholderContext context) {

        BlockState wanted_state = context.getWantedTargetStateForTesting();

        if (wanted_state == null) {
            return false;
        }

        if (context.getCheckTargetPosition()) {
            // Get the current block at the given position
            BlockState current_state = context.getCurrentState();

            // There is no state, so we're probably missing a lot of information
            // Return false just to be safe
            if (current_state == null) {
                return false;
            }

            // If replacement logic is disabled,
            // we have to make sure the target block is empty
            if (!context.getUseTargetReplacementLogic()) {
                // If the current block is not air or a fluid, we can't place it
                if (!current_state.isAir() && !current_state.isLiquid()) {
                    return false;
                }
            }

            wanted_state = context.getWantedTargetState();

            // Let's assume whatever the context has in its item stack form is correct
            if (wanted_state == null) {
                return true;
            }

            if (!wanted_state.canPlaceAt(context.getWorld(), context.getTargetPos())) {
                return false;
            }
        }

        return true;
    }
}
