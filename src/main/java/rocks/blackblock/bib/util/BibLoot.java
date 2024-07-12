package rocks.blackblock.bib.util;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import rocks.blackblock.bib.bv.value.BvList;
import rocks.blackblock.bib.bv.value.BvLootTableSet;
import rocks.blackblock.bib.bv.value.BvTag;
import rocks.blackblock.bib.interfaces.HasItemIcon;

import java.util.HashSet;
import java.util.Set;

/**
 * Library class for working with loot tables
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BibLoot {

    public static BvList<BvLootTableSet> LOOT_TABLES = new BvList<>();
    private static Set<RegistryKey<LootTable>> BUFFER = null;

    /**
     * Register a loot table
     *
     * @since    0.2.0
     */
    private static BvLootTableSet register(RegistryKey<LootTable> key) {
        BvLootTableSet result = new BvLootTableSet(key);
        LOOT_TABLES.add(result);
        return result;
    }

    /**
     * Register a combined loot table set
     *
     * @since    0.2.0
     */
    @SafeVarargs
    private static BvLootTableSet register(RegistryKey<LootTable>... keys) {
        return register(Set.of(keys));
    }

    /**
     * Register a combined loot table set
     *
     * @since    0.2.0
     */
    private static BvLootTableSet register(Set<RegistryKey<LootTable>> keys) {
        BvLootTableSet result = new BvLootTableSet(keys);
        LOOT_TABLES.add(result);
        return result;
    }

    /**
     * Start a buffer
     *
     * @since    0.2.0
     */
    private static void startBuffer() {
        BUFFER = new HashSet<>();
    }

    /**
     * Stop a buffer
     *
     * @since    0.2.0
     */
    private static BvLootTableSet flushBuffer(String title) {
        return register(BUFFER).setTitle(title);
    }

    static {
        register(LootTables.END_CITY_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.END_CITY).setIcon(Items.PURPUR_PILLAR);
        register(LootTables.SIMPLE_DUNGEON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.DUNGEON).setIcon(Items.MOSSY_COBBLESTONE);
        register(LootTables.ABANDONED_MINESHAFT_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.MINESHAFT).setIcon(Items.MINECART);

        register(LootTables.RUINED_PORTAL_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.RUINED_PORTAL).setIcon(Items.CRYING_OBSIDIAN);
        register(LootTables.DESERT_PYRAMID_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.DESERT_PYRAMID).setIcon(Items.SANDSTONE);
        register(LootTables.IGLOO_CHEST_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.IGLOO).setIcon(Items.SNOW_BLOCK);
        register(LootTables.JUNGLE_TEMPLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.JUNGLE_TEMPLE).setIcon(Items.VINE);
        register(LootTables.JUNGLE_TEMPLE_DISPENSER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.JUNGLE_TEMPLE).setIcon(Items.DISPENSER);

        register(LootTables.CAT_MORNING_GIFT_GAMEPLAY).addTags(BibTags.HUSBANDRY, BibTags.PET).setIcon(Items.CAT_SPAWN_EGG);
        register(LootTables.SNIFFER_DIGGING_GAMEPLAY).addTags(BibTags.HUSBANDRY).setIcon(Items.SNIFFER_EGG);
        register(LootTables.PANDA_SNEEZE_GAMEPLAY).addTags(BibTags.HUSBANDRY).setIcon(Items.SNIFFER_EGG);

        register(LootTables.PIGLIN_BARTERING_GAMEPLAY).addTags(BibTags.BASTION_REMNANT).setIcon(Items.PIGLIN_HEAD);
        register(LootTables.NETHER_BRIDGE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.NETHER_FORTRESS).setIcon(Items.NETHER_BRICK).setTitle("Nether fortress");

        startBuffer();
        register(LootTables.VILLAGE_WEAPONSMITH_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.IRON_SWORD);
        register(LootTables.VILLAGE_TOOLSMITH_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.IRON_PICKAXE);
        register(LootTables.VILLAGE_ARMORER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.LEATHER_CHESTPLATE);
        register(LootTables.VILLAGE_CARTOGRAPHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CARTOGRAPHY_TABLE);
        register(LootTables.VILLAGE_MASON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.STONECUTTER);
        register(LootTables.VILLAGE_SHEPARD_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.LOOM);
        register(LootTables.VILLAGE_BUTCHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SMOKER);
        register(LootTables.VILLAGE_FLETCHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.FLETCHING_TABLE);
        register(LootTables.VILLAGE_FISHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.BARREL);
        register(LootTables.VILLAGE_TANNERY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CAULDRON);
        register(LootTables.VILLAGE_TEMPLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.BREWING_STAND);
        register(LootTables.VILLAGE_DESERT_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SAND);
        register(LootTables.VILLAGE_PLAINS_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.OAK_LOG);
        register(LootTables.VILLAGE_TAIGA_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SPRUCE_LEAVES);
        register(LootTables.VILLAGE_SNOWY_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SNOW);
        flushBuffer("All Village Chests").addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.STRONGHOLD_CORRIDOR_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.STONE_BRICKS);
        register(LootTables.STRONGHOLD_CROSSING_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.STONE_BRICKS);
        register(LootTables.STRONGHOLD_LIBRARY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.BOOKSHELF);
        flushBuffer("All Stronghold Chests").addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.BASTION_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.GILDED_BLACKSTONE);
        register(LootTables.BASTION_OTHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        register(LootTables.BASTION_BRIDGE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        register(LootTables.BASTION_HOGLIN_STABLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        flushBuffer("All Bastion Remnant Chests").addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.SHIPWRECK_MAP_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.MAP);
        register(LootTables.SHIPWRECK_SUPPLY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.MOSS_BLOCK);
        register(LootTables.SHIPWRECK_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.HEART_OF_THE_SEA);
        flushBuffer("All Shipwreck Chests").addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.WOODLAND_MANSION_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.WOODLAND_MANSION).setIcon(Items.DARK_OAK_LOG);
        register(LootTables.PILLAGER_OUTPOST_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.PILLAGER_OUTPOST).setIcon(Items.CROSSBOW);
        flushBuffer("All Pillager-Related Chests").addTags(BibTags.LOOT_CHEST).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.TRIAL_CHAMBERS_REWARD_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.GOLD_INGOT);
        register(LootTables.TRIAL_CHAMBERS_REWARD_COMMON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.EMERALD);
        register(LootTables.TRIAL_CHAMBERS_REWARD_RARE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND);
        register(LootTables.TRIAL_CHAMBERS_REWARD_UNIQUE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_BLOCK);
        register(LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.GOLD_INGOT);
        register(LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.EMERALD);
        register(LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND);
        register(LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_BLOCK);
        register(LootTables.TRIAL_CHAMBERS_SUPPLY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.IRON_SWORD);
        register(LootTables.TRIAL_CHAMBERS_CORRIDOR_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.IRON_PICKAXE);
        register(LootTables.TRIAL_CHAMBERS_INTERSECTION_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.LEATHER_CHESTPLATE);
        register(LootTables.TRIAL_CHAMBERS_INTERSECTION_BARREL_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.BARREL);
        register(LootTables.TRIAL_CHAMBERS_ENTRANCE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.COPPER_DOOR);
        register(LootTables.TRIAL_CHAMBERS_CORRIDOR_POT).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DECORATED_POT);
        flushBuffer("All Trial Chamber Chests").addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.TRIAL_CHAMBERS_CORRIDOR_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        register(LootTables.TRIAL_CHAMBERS_CHAMBER_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        register(LootTables.TRIAL_CHAMBERS_WATER_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        flushBuffer("All Trial Chamber Traps").addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);

        register(LootTables.TRIAL_CHAMBER_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_CHESTPLATE);
        register(LootTables.TRIAL_CHAMBER_RANGED_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.CROSSBOW);
        register(LootTables.TRIAL_CHAMBER_MELEE_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_SWORD);

        startBuffer();
        register(LootTables.FISHING_FISH_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.COD);
        register(LootTables.FISHING_JUNK_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.LEATHER_BOOTS);
        register(LootTables.FISHING_TREASURE_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.NAME_TAG);
        flushBuffer("All Fishing Loot").addTags(BibTags.FISHING).setIcon(Items.CHEST);

        startBuffer();
        register(LootTables.DESERT_WELL_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.WATER_BUCKET);
        register(LootTables.DESERT_PYRAMID_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.SAND);
        register(LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.TERRACOTTA);
        register(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.DIAMOND);
        register(LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.BRAIN_CORAL_BLOCK);
        register(LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.BLUE_ICE);
        flushBuffer("All Archeology Loot").addTags(BibTags.ARCHEOLOGY).setIcon(Items.CHEST);

        startBuffer();
        //Registry.register(Registries.ENTITY_TYPE
        Registries.ENTITY_TYPE.forEach(entityType -> {
            var loot_table_key = entityType.getLootTableId();

            if (loot_table_key == null) {
                return;
            }

            var group = entityType.getSpawnGroup();
            var with_item_icon = (HasItemIcon) entityType;
            BvLootTableSet loot_table_set;

            if (group == SpawnGroup.MONSTER) {
                loot_table_set = register(loot_table_key).addTags(BibTags.ENTITY, BibTags.MONSTER).setTitle(entityType.getName().getString());
            } else {
                loot_table_set = register(loot_table_key).addTags(BibTags.ENTITY).setTitle(entityType.getName().getString());
            }

            loot_table_set.setIcon(with_item_icon.getItemIcon());
        });

        flushBuffer("All Entities").addTags(BibTags.ENTITY).setIcon(Items.CHEST);
    }

    /**
     * Don't let anyone instantiate this class
     *
     * @since    0.2.0
     */
    private BibLoot() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }



}
