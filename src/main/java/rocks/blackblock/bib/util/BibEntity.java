package rocks.blackblock.bib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Library class for working with entities
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibEntity {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibEntity() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Is this entity only holding garbage?
     * Holding nothing will also return true
     * @since    0.2.0
     */
    public static boolean isOnlyHoldingGarbage(LivingEntity entity) {

        if (entity == null || !entity.isAlive()) {
            return true;
        }

        for (var slot : EquipmentSlot.values()) {

            ItemStack stack = entity.getEquippedStack(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (BibItem.isGarbage(stack)) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Does this entity have a roof above it?
     * @since    0.2.0
     */
    public static boolean hasRoofAbove(Entity entity) {
        BlockPos pos = entity.getBlockPos();
        return !entity.getWorld().isSkyVisible(pos);
    }

    /**
     * Does this entity have a roof above it?
     * @since    0.2.0
     */
    public static boolean hasRoofAbove(Entity entity, int max_y_range) {

        if (!hasRoofAbove(entity)) {
            return false;
        }

        return hasRoofAbove(entity.getWorld(), entity.getBlockPos().mutableCopy(), entity.getBlockPos().getY(), max_y_range);
    }

    /**
     * Does this entity have a roof above it?
     * @since    0.2.0
     */
    private static boolean hasRoofAbove(World world, BlockPos.Mutable mutable, int start_y, int max_y_range) {
        for (int y = start_y + 1; y < start_y + max_y_range; y++) {
            mutable.setY(y);
            if (world.getBlockState(mutable).isOpaque()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is this entity enclosed horizontally?
     * @since    0.2.0
     */
    private static boolean isEnclosedHorizontally(World world, BlockPos.Mutable mutable, BlockPos center) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : directions) {
            if (!hasWallInDirection(world, mutable, center, dir[0], dir[1])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Does this entity have a wall in the given direction?
     * @since    0.2.0
     */
    private static boolean hasWallInDirection(World world, BlockPos.Mutable mutable, BlockPos center, int dx, int dz) {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        for (int i = 1; i <= 10; i++) {
            x += dx;
            z += dz;

            boolean has_wall = true;
            for (int j = -1; j <= 2; j++) {
                mutable.set(x, y + j, z);

                if (!world.getBlockState(mutable).isOpaque()) {
                    has_wall = false;
                    break;
                }
            }

            if (has_wall) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is this entity in an enclosed space?
     * @since    0.2.0
     */
    public static boolean isInEnclosedSpace(Entity entity) {

        World world = entity.getWorld();
        BlockPos pos = entity.getBlockPos();
        BlockPos.Mutable mutable = pos.mutableCopy();
        int start_y = pos.getY();
        int max_y_range = 20;

        if (start_y > 60) {
            max_y_range = 35;
        }

        // Check for a solid roof above the player
        if (!hasRoofAbove(world, mutable, start_y, max_y_range)) {
            return false;
        }

        // Check for walls around the player
        return isEnclosedHorizontally(world, mutable, pos);
    }

    /**
     * Is this entity in a cave?
     * @since    0.2.0
     */
    public static boolean isInCave(Entity entity) {

        World world = entity.getWorld();
        BlockPos pos = entity.getBlockPos();
        int start_y = pos.getY();

        if (start_y > 70) {
            return false;
        }

        BlockPos.Mutable mutable = pos.mutableCopy();

        // Check for a solid roof above the player
        if (world.isSkyVisible(mutable)) {
            return false;
        }

        int radius = 5;

        if (start_y > 60) {
            radius = 15;
        }

        boolean has_ceiling = BibPos.spiralAroundPosition(pos.getX(), pos.getZ(), radius, (x, z) -> world.isSkyVisible(mutable.set(x, start_y, z)));

        return has_ceiling;
    }
}
