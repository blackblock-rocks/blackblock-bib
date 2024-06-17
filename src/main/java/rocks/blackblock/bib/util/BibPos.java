package rocks.blackblock.bib.util;

import net.minecraft.util.math.*;

/**
 * Library class for working with positions & vectors
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibPos {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibPos() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static BlockPos getBlockPos(Vec3i vec) {
        return new BlockPos(vec);
    }

    /**
     * Get a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static BlockPos getBlockPos(Vec3d vec) {
        return getBlockPos(vec.x, vec.y, vec.z);
    }

    /**
     * Get a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static BlockPos getBlockPos(Position pos) {
        return getBlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Get a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static BlockPos getBlockPos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    /**
     * Get a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static BlockPos getBlockPos(double x, double y, double z) {
        return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    /**
     * Get a Vec3i
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Vec3i getVec3i(BlockPos pos) {
        return pos;
    }

    /**
     * Get a Vec3i
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Vec3i getVec3i(float x, float y, float z) {
        return getVec3i(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    /**
     * Get a Vec3i
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Vec3i getVec3i(double x, double y, double z) {
        return getVec3i(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    /**
     * Get a Vec3i
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Vec3i getVec3i(int x, int y, int z) {
        return new Vec3i(x, y, z);
    }

    /**
     * Get a Vec3i
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Vec3i getVec3i(Position pos) {
        return getVec3i(pos.getX(), pos.getY(), pos.getZ());
    }

}
