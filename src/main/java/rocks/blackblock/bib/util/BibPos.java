package rocks.blackblock.bib.util;

import net.minecraft.nbt.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Parse a BlockPos from an NBT element
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static BlockPos parseBlockPos(NbtElement element) {

        if (element == null) {
            return null;
        }

        if (element instanceof NbtIntArray nbt_int_array) {
            var int_arr = nbt_int_array.getIntArray();
            return new BlockPos(int_arr[0], int_arr[1], int_arr[2]);
        }

        if (element instanceof NbtCompound compound) {
            if (compound.contains("X") && compound.contains("Y") && compound.contains("Z")) {
                return new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
            }
        }

        return null;
    }

    /**
     * Serialize a BlockPos to an NBT element
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static NbtElement serializeBlockPos(BlockPos pos) {
        return NbtHelper.fromBlockPos(pos);
    }

    /**
     * Serialize a ChunkPos to an NBT element
     *
     * @since    0.2.0
     */
    @Nullable
    public static NbtIntArray serializeChunkPos(ChunkPos pos) {

        if (pos == null) {
            return null;
        }

        return new NbtIntArray(new int[]{pos.x, pos.z});
    }

    /**
     * Parse a ChunkPos from an NBT element
     *
     * @since    0.2.0
     */
    @Nullable
    public static ChunkPos parseChunkPos(NbtElement element) {

        if (element == null) {
            return null;
        }

        if (element instanceof NbtIntArray nbt_int_array) {
            var int_arr = nbt_int_array.getIntArray();

            if (int_arr.length == 2) {
                return new ChunkPos(int_arr[0], int_arr[1]);
            }

            // It's probably a BlockPos?
            return new ChunkPos(int_arr[0], int_arr[2]);
        }

        if (element instanceof NbtCompound compound) {
            if (compound.contains("X") && compound.contains("Z")) {
                return new ChunkPos(compound.getInt("X"), compound.getInt("Z"));
            }
        }

        return null;
    }

    /**
     * Return the X coordinate from a long ChunkPos value
     * @since    0.2.0
     */
    public static int getChunkX(long chunk_pos) {
        return (int) chunk_pos;
    }

    /**
     * Return the Z coordinate from a long ChunkPos value
     * @since    0.2.0
     */
    public static int getChunkZ(long chunk_pos) {
        return (int) (chunk_pos >> 32);
    }

    /**
     * Turn an X and Z coordinate into a long ChunkPos value:
     * The first 32 bits represent the x-coordinate and
     * the last 32 bits represent the z-coordinate
     * @since    0.2.0
     */
    public static long toLong(int chunk_x, int chunk_z) {
        return ((long) chunk_z << 32) | (chunk_x & 0xFFFFFFFFL);
    }

    /**
     * Spiral around a position.
     * @since    0.2.0
     */
    public static boolean spiralAroundPosition(int center_x, int center_z, int radius, PositionConsumer action) {
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        int max_steps = (2 * radius + 1) * (2 * radius + 1);

        for (int i = 0; i < max_steps; i++) {
            if ((-radius <= x && x <= radius) && (-radius <= z && z <= radius)) {

                // Skip the center
                if (x != 0 || z != 0) {
                    if (!action.accept(center_x + x, center_z + z)) {
                        return false;
                    }
                }
            }

            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                // Change direction
                int temp = dx;
                dx = -dz;
                dz = temp;
            }

            x += dx;
            z += dz;
        }

        return true;
    }

    /**
     * Get the squared distance between 2 positions
     * @since    0.2.0
     */
    public static double getSquaredDistance(double x1, double z1, double x2, double z2) {
        final double dx = x1 - x2;
        final double dy = z1 - z2;
        return dx * dx + dy * dy;
    }

    /**
     * Position consumer interface
     * @since    0.2.0
     */
    @FunctionalInterface
    public interface PositionConsumer {
        boolean accept(int x, int z);
    }
}
