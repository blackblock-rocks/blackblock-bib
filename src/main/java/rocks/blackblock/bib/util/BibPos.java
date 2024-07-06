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
}
