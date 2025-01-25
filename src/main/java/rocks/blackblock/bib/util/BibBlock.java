package rocks.blackblock.bib.util;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.interaction.InternalBibBlockBaseInteraction;
import rocks.blackblock.bib.interaction.InternalBibBlockInteraction;
import rocks.blackblock.bib.interaction.InternalBibBlockWithItemInteraction;
import rocks.blackblock.bib.mixin.LockableContainerBlockEntityAccessor;

/**
 * Library class for working with Blocks, BlockStates & BlockEntities
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibBlock {

    /**
     * The base Block Interaction class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public abstract static class BaseInteraction extends InternalBibBlockBaseInteraction {
        public BaseInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            super(state, world, pos, player, hit);
        }

        /**
         * Get ourselves as a BibBlock.BaseInteraction class
         * Basically just for the compiler
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        @ApiStatus.Internal
        @Override
        protected BibBlock.BaseInteraction getSelfAsBaseInteraction() {
            return this;
        }
    }

    /**
     * Our own Block Interaction class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Interaction extends InternalBibBlockInteraction {
        public Interaction(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            super(state, world, pos, player, hit);
        }
    }

    /**
     * Our own Block Interaction With Item class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class InteractionWithItem extends InternalBibBlockWithItemInteraction {
        public InteractionWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            super(stack, state, world, pos, player, hand, hit);
        }
    }

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibBlock() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Set the (custom) name of a LockableContainerBlockEntity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setName(@NotNull LockableContainerBlockEntity block_entity, Text text) {
        ((LockableContainerBlockEntityAccessor) block_entity).setCustomName(text);
    }

    /**
     * Get the (custom) name of a Nameable BlockEntity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Text getName(Nameable instance) {
        return instance.getName();
    }

    /**
     * Create new Block Settings
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.3.0
     */
    public static AbstractBlock.Settings createBlockSettings() {
        return AbstractBlock.Settings.create();
    }

    /**
     * Create new Block Settings based on the given block
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.3.0
     */
    public static AbstractBlock.Settings createBlockSettings(AbstractBlock block) {
        return AbstractBlock.Settings.copy(block);
    }

    /**
     * Set the BlockEntity data in the given ItemStack.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setBlockEntityData(ItemStack target, BlockEntity block_entity, NbtCompound data) {
        BlockItem.setBlockEntityData(target, block_entity.getType(), data);
    }

    /**
     * Set the BlockEntity data in the given ItemStack.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setBlockEntityData(ItemStack target, BlockEntity block_entity) {

        if (block_entity == null) {
            return;
        }

        World world = block_entity.getWorld();
        DynamicRegistryManager registry_manager;

        if (world != null) {
            registry_manager = world.getRegistryManager();
        } else {
            registry_manager = BibMod.getDynamicRegistry();
        }

        NbtCompound nbtCompound = block_entity.createComponentlessNbt(registry_manager);
        block_entity.removeFromCopiedStackNbt(nbtCompound);
        BlockItem.setBlockEntityData(target, block_entity.getType(), nbtCompound);
        target.applyComponentsFrom(block_entity.createComponentMap());
    }

    /**
     * Set the BlockEntity data in the given ItemStack.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setBlockEntityData(ItemStack target, BlockEntityType<?> type, NbtCompound data) {
        BlockItem.setBlockEntityData(target, type, data);
    }

    /**
     * Get the BlockEntity data of the given ItemStack.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static NbtCompound getBlockEntityData(ItemStack source) {

        NbtComponent nbtComponent = source.get(DataComponentTypes.BLOCK_ENTITY_DATA);

        if (nbtComponent == null) {
            return null;
        }

        return nbtComponent.getNbt();
    }

    /**
     * Get the comparator output of an expected BlockEntity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static int calculateComparatorOutput(BlockState state, World world, BlockPos pos) {
        return BibBlock.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    /**
     * Get the comparator output of an expected BlockEntity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static int calculateComparatorOutput(@Nullable BlockEntity entity) {

        if (entity instanceof Inventory block_inv) {
            return BibInventory.calculateComparatorOutput(block_inv);
        }

        return 0;
    }

    /**
     * Is the given block mostly air?
     * @since    0.2.0
     */
    public static boolean isMostlyAir(Block block) {

        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
            return true;
        }

        if (block instanceof LeavesBlock) {
            return true;
        }

        if (block instanceof ShortPlantBlock) {
            return true;
        }

        if (block instanceof TallPlantBlock) {
            return true;
        }

        if (block instanceof CactusBlock) {
            return true;
        }

        if (block instanceof SugarCaneBlock) {
            return true;
        }

        return false;
    }

    /**
     * Is the given blockstate mostly air?
     * @since    0.2.0
     */
    public static boolean isMostlyAir(BlockState state) {

        if (state.isAir() || state.isIn(BlockTags.FIRE)) {
            return true;
        }

        return isMostlyAir(state.getBlock());
    }

    /**
     * Interaction result enum
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public enum InteractionResult {
        SUCCESS(ActionResult.SUCCESS),
        SUCCESS_NO_ITEM_USED(ActionResult.SUCCESS.noIncrementStat()),
        CONSUME(ActionResult.CONSUME),
        CONSUME_PARTIAL(ActionResult.CONSUME.noIncrementStat()),
        PASS(ActionResult.PASS),
        FAIL(ActionResult.FAIL);

        public final ActionResult actual_result;

        InteractionResult(ActionResult actual_result) {
            this.actual_result = actual_result;
        }

        public ActionResult getVanillaResult() {
            return this.actual_result;
        }
    }

    /**
     * Interaction with item result enum
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public enum InteractionWithItemResult {

        // SUCCESS used to be used for server-side success,
        // but now SUCCESS_SERVER is added instead. So now we have
        // the confusing situation where our SUCCESS is the opposite of the vanilla one
        SUCCESS(ActionResult.SUCCESS_SERVER),
        SUCCESS_CLIENT(ActionResult.SUCCESS),
        CONSUME(ActionResult.CONSUME),
        // CONSUME_PARTIAL no longer exists, but it wasn't exactly used either
        CONSUME_PARTIAL(ActionResult.CONSUME),
        PASS_TO_DEFAULT_BLOCK_INTERACTION(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION),

        // SKIP_DEFAULT_BLOCK_INTERACTION no longer exists,
        // most uses have been replaced with ActionResult.PASS
        SKIP_DEFAULT_BLOCK_INTERACTION(ActionResult.PASS),
        FAIL(ActionResult.FAIL);

        public final ActionResult actual_result;

        InteractionWithItemResult(ActionResult actual_result) {
            this.actual_result = actual_result;
        }

        public ActionResult getVanillaResult() {
            return this.actual_result;
        }
    }

    /**
     * Block/BlockEntities with this interface will have a
     * custom method to handle an interaction
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface InteractionHandler {
        void handleBlockInteraction(BibBlock.Interaction interaction);
    }

    /**
     * Block/BlockEntities with this interface will have a
     * custom method to handle an interaction (with an item)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface InteractionWithItemHandler {
        void handleBlockInteractionWithItem(BibBlock.InteractionWithItem interaction);
    }

    /**
     * Block/BlockEntities with this interface will implement a
     * check to see if the given player can actually open the GUI
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface PlayerGuiPermissionCheck {
        boolean guiCanBeOpenedBy(BibBlock.BaseInteraction interaction);
    }

    /**
     * Block/BlockEntities with this interface will implement a
     * method that returns an identifier of a statistic to increase
     * after an interaction takes place
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface HasInteractionStatistic {
        Identifier getInteractionStatisticIdentifier();
    }

    /**
     * Get an ItemStack representation of the current block
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface ItemStackRepresentation {
        ItemStack getBlockAsItemStackRepresentation();
    }
}
