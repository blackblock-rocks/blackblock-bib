package rocks.blackblock.bib.placeholder;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.CheckedRandom;
import rocks.blackblock.bib.util.BibBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BlockPlaceholderResolvers {

    // Default block placeholder resolvers
    public final static List<BlockPlaceholderResolver> DEFAULT_RESOLVERS = new ArrayList<>();

    /**
     * Register a resolver
     *
     * @since    0.2.0
     */
    public static BlockPlaceholderResolver register(BlockPlaceholderResolver resolver) {
        DEFAULT_RESOLVERS.add(resolver);
        return resolver;
    }

    /**
     * Register a resolver
     *
     * @since    0.2.0
     */
    public static BlockPlaceholderResolver register(String id, Function<PlaceholderContext, PlaceholderContext.Result> resolver) {
        BlockPlaceholderResolver instance = new BlockPlaceholderResolver(id);
        instance.setResolver(resolver);
        return instance;
    }

    // Default bucket resolver
    public final static BlockPlaceholderResolver BUCKET_RESOLVER = register("buckets", placeholderContext -> {
        ItemStack source = placeholderContext.getSourceStack();
        Item item = source.getItem();
        Block block = null;

        if (item == Items.WATER_BUCKET) {
            block = Blocks.WATER;
        } else if (item == Items.LAVA_BUCKET) {
            block = Blocks.LAVA;
        } else if (item == Items.POWDER_SNOW_BUCKET) {
            block = Blocks.POWDER_SNOW;
        } else if (item == Items.BUCKET) {
            block = Blocks.AIR;
        }

        if (block == null) {
            return null;
        }

        return placeholderContext.suggest(block);
    });

    // Non-registered spawn eggs resolver
    public final static BlockPlaceholderResolver SPAWN_EGGS_RESOLVER = new BlockPlaceholderResolver("spawn_eggs").setResolver(placeholderContext -> {

        ItemStack source = placeholderContext.getSourceStack();

        if (source == null) {
            return null;
        }

        Item item = source.getItem();


        if (item instanceof SpawnEggItem spawn_egg_item) {
            EntityType<?> type = spawn_egg_item.getEntityType(placeholderContext.getWorld().getRegistryManager(), source);

            if (type != null) {
                var stack = placeholderContext.getTargetStackSuggestion();

                if (stack == null || stack.isEmpty()) {
                    stack = new ItemStack(Items.SPAWNER);
                }

                var target_item = stack.getItem();
                BlockEntity target_be = null;

                if (target_item == Items.SPAWNER) {
                    var spawner_be = new MobSpawnerBlockEntity(new BlockPos(0, 0, 0), Blocks.SPAWNER.getDefaultState());
                    spawner_be.setEntityType(type, new CheckedRandom(0));
                    target_be = spawner_be;
                } else if (target_item == Items.TRIAL_SPAWNER) {
                    var spawner_be = new TrialSpawnerBlockEntity(new BlockPos(0, 0, 0), Blocks.TRIAL_SPAWNER.getDefaultState());
                    spawner_be.setEntityType(type, new CheckedRandom(0));
                    target_be = spawner_be;
                } else if (target_item == Items.MINECART) {
                    return placeholderContext.suggest((world, pos) -> {
                        MinecartEntity minecart = EntityType.MINECART.spawn(world, pos, SpawnReason.STRUCTURE);
                        var passenger = type.create(world, SpawnReason.STRUCTURE);
                        passenger.startRiding(minecart, true);
                        return true;
                    });
                }

                if (target_be != null) {
                    BibBlock.setBlockEntityData(stack, target_be);
                }

                return placeholderContext.suggest(stack);
            }
        }

        return null;
    });

}
