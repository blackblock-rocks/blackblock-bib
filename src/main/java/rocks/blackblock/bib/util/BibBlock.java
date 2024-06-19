package rocks.blackblock.bib.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
     * Set the BlockEntity data in the given ItemStack.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setBlockEntityData(ItemStack target, BlockEntity block_entity, NbtCompound data) {
        BlockItem.setBlockEntityData(target, block_entity.getType(), data);
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
}
