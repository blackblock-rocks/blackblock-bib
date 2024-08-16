package rocks.blackblock.bib.collection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class to see if a chunk contains a certain block
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class ChunkHasBlockCache {

    public final HashMap<RegistryKey<World>, Long2ObjectMap<ObjectOpenHashSet<BlockPos>>> dimension_map = new HashMap<>();
    private final Set<Block> blocks = new HashSet<>();

    public ChunkHasBlockCache() {

    }

    public ChunkHasBlockCache(@NotNull Block block) {
        this.blocks.add(block);
    }

    public ChunkHasBlockCache(Block[] blocks) {
        this.blocks.addAll(Arrays.asList(blocks));
    }

    /**
     * Add a block to look for
     * @since    0.2.0
     */
    public void addBlock(@NotNull Block block) {

        if (this.blocks.contains(block)) {
            return;
        }

        this.blocks.add(block);
        this.dimension_map.clear();
    }

    /**
     * Get the chunk map for the given world
     * @since    0.2.0
     */
    public Long2ObjectMap<ObjectOpenHashSet<BlockPos>> getChunkMap(World world) {
        return this.getChunkMap(world.getRegistryKey());
    }

    /**
     * Get the chunk map for the given dimension
     * @since    0.2.0
     */
    public Long2ObjectMap<ObjectOpenHashSet<BlockPos>> getChunkMap(RegistryKey<World> world_key) {
        return dimension_map.computeIfAbsent(world_key, k -> new Long2ObjectOpenHashMap<>(100));
    }

    /**
     * Get or calculate the chunk map for the given dimension
     * @since    0.2.0
     */
    public List<BlockPos> getChunkBlocks(World world, Chunk chunk) {
        return this.getChunkBlocks(world.getRegistryKey(), chunk);
    }

    /**
     * Get or calculate the chunk map for the given dimension
     * @since    0.2.0
     */
    public List<BlockPos> getChunkBlocks(RegistryKey<World> world_key, Chunk chunk) {
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> map = getChunkMap(world_key);
        long pos = chunk.getPos().toLong();

        if (!map.containsKey(pos)) {
            recalculateChunk(world_key, chunk);
        }

        return new ArrayList<>(map.get(pos));
    }

    /**
     * Does the given chunk contain any of the blocks in this cache?
     * @since    0.2.0
     */
    public boolean chunkContainsBlock(World world, Chunk chunk) {
        return this.chunkContainsBlock(world.getRegistryKey(), chunk);
    }

    /**
     * Does the given chunk contain any of the blocks in this cache?
     * @since    0.2.0
     */
    public boolean chunkContainsBlock(RegistryKey<World> world_key, Chunk chunk) {
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> map = getChunkMap(world_key);
        long pos = chunk.getPos().toLong();

        if (!map.containsKey(pos)) {
            recalculateChunk(world_key, chunk);
        }

        return !map.get(pos).isEmpty();
    }

    /**
     * Add the given block_pos to the map
     * @since    0.2.0
     */
    public void addBlockPos(World world, BlockPos block_pos) {
        this.addBlockPos(world.getRegistryKey(), world.getChunk(block_pos), block_pos);
    }

    /**
     * Add the given block_pos to the map
     * @since    0.2.0
     */
    public void addBlockPos(RegistryKey<World> world_key, Chunk chunk, BlockPos block_pos) {
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> chunk_map = getChunkMap(world_key);
        long chunk_pos = chunk.getPos().toLong();

        ObjectOpenHashSet<BlockPos> positions = chunk_map.computeIfAbsent(chunk_pos, k -> new ObjectOpenHashSet<>());
        positions.add(block_pos);
    }

    /**
     * Add the given block_pos to the map
     * @since    0.2.0
     */
    public void addBlockPos(World world, Chunk chunk, BlockPos block_pos) {
        this.addBlockPos(world.getRegistryKey(), chunk, block_pos);
    }

    /**
     * Remove the given block_pos from the map
     * @since    0.2.0
     */
    public void removeBlockPos(World world, BlockPos block_pos) {
        this.removeBlockPos(world.getRegistryKey(), world.getChunk(block_pos), block_pos);
    }

    /**
     * Remove the given block_pos from the map
     * @since    0.2.0
     */
    public void removeBlockPos(World world, Chunk chunk, BlockPos block_pos) {
        this.removeBlockPos(world.getRegistryKey(), chunk, block_pos);
    }

    /**
     * Remove the given block_pos from the map
     * @since    0.2.0
     */
    public void removeBlockPos(RegistryKey<World> world_key, Chunk chunk, BlockPos block_pos) {
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> chunk_map = getChunkMap(world_key);
        long chunk_pos = chunk.getPos().toLong();

        ObjectOpenHashSet<BlockPos> positions = chunk_map.get(chunk_pos);
        if (positions != null) {
            positions.remove(block_pos);
        }
    }

    /**
     * Recalculate the given chunk
     * @since    0.2.0
     */
    public List<BlockPos> recalculateChunk(World world, Chunk chunk) {
        return this.recalculateChunk(world.getRegistryKey(), chunk);
    }

    /**
     * Recalculate the given chunk
     * @since    0.2.0
     */
    public List<BlockPos> recalculateChunk(RegistryKey<World> world_key, Chunk chunk) {
        ObjectOpenHashSet<BlockPos> positions = new ObjectOpenHashSet<>();
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> chunk_map = getChunkMap(world_key);

        ChunkPos chunk_pos = chunk.getPos();
        long chunk_pos_long = chunk_pos.toLong();

        ChunkSection[] sections = chunk.getSectionArray();
        int y_section_index = (chunk.getBottomY() >> 4) - 1;

        for (ChunkSection section : sections) {
            y_section_index++;

            if (section == null || section.isEmpty()) {
                continue;
            }

            int y_offset = y_section_index * 16;

            if (!sectionContainsTargetBlocks(section)) {
                continue;
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        Block block = state.getBlock();

                        for (Block block_to_check : this.blocks) {
                            if (block == block_to_check) {
                                BlockPos block_pos_in_world = chunk_pos.getBlockPos(x, y + y_offset, z);
                                positions.add(block_pos_in_world);
                            }
                        }
                    }
                }
            }
        }

        chunk_map.put(chunk_pos_long, positions);

        return new ArrayList<>(positions);
    }

    private boolean sectionContainsTargetBlocks(ChunkSection section) {
        return section.getBlockStateContainer().hasAny(blockState -> {
            for (Block block_to_check : this.blocks) {
                if (blockState.getBlock() == block_to_check) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Get a list of all block positions in the given chunk
     * @since   0.2.0
     */
    public List<BlockPos> getBlockPositions(World world, Chunk chunk) {
        return this.getBlockPositions(world.getRegistryKey(), chunk);
    }

    /**
     * Get a list of all block positions in the given chunk
     */
    public List<BlockPos> getBlockPositions(RegistryKey<World> world_key, Chunk chunk) {
        Long2ObjectMap<ObjectOpenHashSet<BlockPos>> chunk_map = getChunkMap(world_key);
        ObjectOpenHashSet<BlockPos> blocks = chunk_map.get(chunk.getPos().toLong());

        if (blocks == null) {
            return recalculateChunk(world_key, chunk);
        }

        return new ArrayList<>(blocks);
    }
}
