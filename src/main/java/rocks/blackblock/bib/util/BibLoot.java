package rocks.blackblock.bib.util;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import rocks.blackblock.bib.bv.value.BvList;
import rocks.blackblock.bib.bv.value.BvLootTableSet;
import rocks.blackblock.bib.interfaces.HasItemIcon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Library class for working with loot tables
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BibLoot {

    public static Map<String, BvLootTableSet> LOOT_TABLES_BY_ID = new HashMap<>();
    public static BvList<BvLootTableSet> LOOT_TABLES = new BvList<>();
    private static Set<RegistryKey<LootTable>> BUFFER = null;

    /**
     * Register a loot table
     *
     * @since    0.2.0
     */
    private static BvLootTableSet register(String id, RegistryKey<LootTable> key) {
        BvLootTableSet result = new BvLootTableSet(key);
        result.setRefId(id);
        LOOT_TABLES.add(result);
        LOOT_TABLES_BY_ID.put(id, result);
        return result;
    }

    /**
     * Register a combined loot table set
     *
     * @since    0.2.0
     */
    @SafeVarargs
    private static BvLootTableSet register(String id, RegistryKey<LootTable>... keys) {
        return register(id, Set.of(keys));
    }

    /**
     * Register a combined loot table set
     *
     * @since    0.2.0
     */
    private static BvLootTableSet register(String id, Set<RegistryKey<LootTable>> keys) {
        BvLootTableSet result = new BvLootTableSet(keys);
        result.setRefId(id);
        LOOT_TABLES.add(result);
        LOOT_TABLES_BY_ID.put(id, result);
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
    private static BvLootTableSet flushBuffer(String id, String title) {
        return register(id, BUFFER).setTitle(title);
    }

    static {
        register("end_city_treasure_chast", LootTables.END_CITY_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.END_CITY).setIcon(Items.PURPUR_PILLAR);
        register("simple_dungeon_chest", LootTables.SIMPLE_DUNGEON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.DUNGEON).setIcon(Items.MOSSY_COBBLESTONE);
        register("abandoned_mineshaft_chest", LootTables.ABANDONED_MINESHAFT_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.MINESHAFT).setIcon(Items.MINECART);

        register("ruined_portal_chest", LootTables.RUINED_PORTAL_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.RUINED_PORTAL).setIcon(Items.CRYING_OBSIDIAN);
        register("desert_pyramid_chest", LootTables.DESERT_PYRAMID_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.DESERT_PYRAMID).setIcon(Items.SANDSTONE);
        register("igloo_chest", LootTables.IGLOO_CHEST_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.IGLOO).setIcon(Items.SNOW_BLOCK);
        register("jungle_temple_chest", LootTables.JUNGLE_TEMPLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.JUNGLE_TEMPLE).setIcon(Items.VINE);
        register("jungle_temple_dispenser", LootTables.JUNGLE_TEMPLE_DISPENSER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.JUNGLE_TEMPLE).setIcon(Items.DISPENSER);

        register("cat_morning_gift", LootTables.CAT_MORNING_GIFT_GAMEPLAY).addTags(BibTags.HUSBANDRY, BibTags.PET).setIcon(Items.CAT_SPAWN_EGG);
        register("sniffer_digging", LootTables.SNIFFER_DIGGING_GAMEPLAY).addTags(BibTags.HUSBANDRY).setIcon(Items.SNIFFER_EGG);
        register("panda_sneeze", LootTables.PANDA_SNEEZE_GAMEPLAY).addTags(BibTags.HUSBANDRY).setIcon(Items.SNIFFER_EGG);

        register("piglin_bartering", LootTables.PIGLIN_BARTERING_GAMEPLAY).addTags(BibTags.BASTION_REMNANT).setIcon(Items.PIGLIN_HEAD);
        register("nether_bridge_chest", LootTables.NETHER_BRIDGE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.NETHER_FORTRESS).setIcon(Items.NETHER_BRICK).setTitle("Nether fortress");

        startBuffer();
        register("weaponsmith_chest", LootTables.VILLAGE_WEAPONSMITH_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.IRON_SWORD);
        register("toolsmith_chest", LootTables.VILLAGE_TOOLSMITH_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.IRON_PICKAXE);
        register("armorer_chest", LootTables.VILLAGE_ARMORER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.LEATHER_CHESTPLATE);
        register("cartographer_chest", LootTables.VILLAGE_CARTOGRAPHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CARTOGRAPHY_TABLE);
        register("mason_chest", LootTables.VILLAGE_MASON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.STONECUTTER);
        register("shepard_chest", LootTables.VILLAGE_SHEPARD_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.LOOM);
        register("butcher_chest", LootTables.VILLAGE_BUTCHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SMOKER);
        register("fletcher_chest", LootTables.VILLAGE_FLETCHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.FLETCHING_TABLE);
        register("fisher_chest", LootTables.VILLAGE_FISHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.BARREL);
        register("tannery_chest", LootTables.VILLAGE_TANNERY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CAULDRON);
        register("temple_chest", LootTables.VILLAGE_TEMPLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.BREWING_STAND);
        register("desert_house_chest", LootTables.VILLAGE_DESERT_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SAND);
        register("plains_chest", LootTables.VILLAGE_PLAINS_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.OAK_LOG);
        register("taiga_house_chest", LootTables.VILLAGE_TAIGA_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SPRUCE_LEAVES);
        register("snowy_house_chest", LootTables.VILLAGE_SNOWY_HOUSE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.SNOW);
        flushBuffer("all_village_chests", "All Village Chests").addTags(BibTags.LOOT_CHEST, BibTags.VILLAGE).setIcon(Items.CHEST);

        startBuffer();
        register("stronghold_corridor_chest", LootTables.STRONGHOLD_CORRIDOR_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.STONE_BRICKS);
        register("stronghold_crossing_chest", LootTables.STRONGHOLD_CROSSING_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.STONE_BRICKS);
        register("stronghold_library_chest", LootTables.STRONGHOLD_LIBRARY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.BOOKSHELF);
        flushBuffer("all_stronghold_chests", "All Stronghold Chests").addTags(BibTags.LOOT_CHEST, BibTags.STRONGHOLD).setIcon(Items.CHEST);

        startBuffer();
        register("bastion_treasure_chest", LootTables.BASTION_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.GILDED_BLACKSTONE);
        register("bastion_other_chest", LootTables.BASTION_OTHER_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        register("bastion_bridge_chest", LootTables.BASTION_BRIDGE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        register("bastion_hoglin_stable_chest", LootTables.BASTION_HOGLIN_STABLE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.BLACKSTONE);
        flushBuffer("all_bestion_chests", "All Bastion Remnant Chests").addTags(BibTags.LOOT_CHEST, BibTags.BASTION_REMNANT).setIcon(Items.CHEST);

        startBuffer();
        register("shipwrek_map_chest", LootTables.SHIPWRECK_MAP_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.MAP);
        register("shipwrek_supply_chest", LootTables.SHIPWRECK_SUPPLY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.MOSS_BLOCK);
        register("shipwrek_treasure_chest", LootTables.SHIPWRECK_TREASURE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.HEART_OF_THE_SEA);
        flushBuffer("all_shipwreck_chests", "All Shipwreck Chests").addTags(BibTags.LOOT_CHEST, BibTags.SHIPWRECK).setIcon(Items.CHEST);

        startBuffer();
        register("woodland_mansion_chest", LootTables.WOODLAND_MANSION_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.WOODLAND_MANSION).setIcon(Items.DARK_OAK_LOG);
        register("pillager_outpost_chest", LootTables.PILLAGER_OUTPOST_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.PILLAGER_OUTPOST).setIcon(Items.CROSSBOW);
        flushBuffer("all_pillager_related_chests", "All Pillager-Related Chests").addTags(BibTags.LOOT_CHEST).setIcon(Items.CHEST);

        startBuffer();
        register("tc_reward", LootTables.TRIAL_CHAMBERS_REWARD_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.GOLD_INGOT);
        register("tc_reward_common", LootTables.TRIAL_CHAMBERS_REWARD_COMMON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.EMERALD);
        register("tc_reward_rare", LootTables.TRIAL_CHAMBERS_REWARD_RARE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND);
        register("tc_reward_unique", LootTables.TRIAL_CHAMBERS_REWARD_UNIQUE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_BLOCK);
        register("tc_reward_ominous", LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.GOLD_INGOT);
        register("tc_reward_ominous_common", LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.EMERALD);
        register("tc_reward_ominous_rare", LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND);
        register("tc_reward_ominous_unique", LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_BLOCK);
        register("tc_supply", LootTables.TRIAL_CHAMBERS_SUPPLY_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.IRON_SWORD);
        register("tc_corridor_chest", LootTables.TRIAL_CHAMBERS_CORRIDOR_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.IRON_PICKAXE);
        register("tc_intersection_chest", LootTables.TRIAL_CHAMBERS_INTERSECTION_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.LEATHER_CHESTPLATE);
        register("tc_intersection_barrel", LootTables.TRIAL_CHAMBERS_INTERSECTION_BARREL_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.BARREL);
        register("tc_entrance_chest", LootTables.TRIAL_CHAMBERS_ENTRANCE_CHEST).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.COPPER_DOOR);
        register("tc_corridor_pot", LootTables.TRIAL_CHAMBERS_CORRIDOR_POT).addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.DECORATED_POT);
        flushBuffer("all_trial_chamber_chests", "All Trial Chamber Chests").addTags(BibTags.LOOT_CHEST, BibTags.TRIAL_CHAMBER).setIcon(Items.CHEST);

        startBuffer();
        register("tc_corridor_dispenser", LootTables.TRIAL_CHAMBERS_CORRIDOR_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        register("tc_chamber_dispenser", LootTables.TRIAL_CHAMBERS_CHAMBER_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        register("tc_water_dispenser", LootTables.TRIAL_CHAMBERS_WATER_DISPENSER).addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);
        flushBuffer("all_trial_chamber_traps", "All Trial Chamber Traps").addTags(BibTags.TRAPS, BibTags.TRIAL_CHAMBER).setIcon(Items.DISPENSER);

        register("tc_equipment", LootTables.TRIAL_CHAMBER_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_CHESTPLATE);
        register("tc_equipment_ranged", LootTables.TRIAL_CHAMBER_RANGED_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.CROSSBOW);
        register("tc_equipment_melee", LootTables.TRIAL_CHAMBER_MELEE_EQUIPMENT).addTags(BibTags.EQUIPMENT, BibTags.TRIAL_CHAMBER).setIcon(Items.DIAMOND_SWORD);

        startBuffer();
        register("fishing_fish", LootTables.FISHING_FISH_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.COD);
        register("fishing_junk", LootTables.FISHING_JUNK_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.LEATHER_BOOTS);
        register("fishing_treasure", LootTables.FISHING_TREASURE_GAMEPLAY).addTags(BibTags.FISHING).setIcon(Items.NAME_TAG);
        flushBuffer("all_fishing_loot", "All Fishing Loot").addTags(BibTags.FISHING).setIcon(Items.CHEST);

        startBuffer();
        register("arch_desert_well", LootTables.DESERT_WELL_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.WATER_BUCKET);
        register("arch_desert_pyramid", LootTables.DESERT_PYRAMID_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.SAND);
        register("arch_trail_common", LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.TERRACOTTA);
        register("arch_trail_rare", LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.DIAMOND);
        register("arch_ocean_ruin_warm", LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.BRAIN_CORAL_BLOCK);
        register("arch_ocean_ruin_cold", LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY).addTags(BibTags.ARCHEOLOGY).setIcon(Items.BLUE_ICE);
        flushBuffer("all_archeology_loot", "All Archeology Loot").addTags(BibTags.ARCHEOLOGY).setIcon(Items.CHEST);

        Set<RegistryKey<LootTable>> monsters = new HashSet<>();

        startBuffer();
        Registries.ENTITY_TYPE.forEach(entityType -> {
            var loot_table_key = entityType.getLootTableId();

            if (loot_table_key == null) {
                return;
            }

            var group = entityType.getSpawnGroup();
            var with_item_icon = (HasItemIcon) entityType;
            BvLootTableSet loot_table_set;

            String name = entityType.getName().getString();
            String id = "entity_" + name;

            if (group == SpawnGroup.MONSTER) {
                monsters.add(loot_table_key);
                loot_table_set = register(name, loot_table_key).addTags(BibTags.ENTITY, BibTags.MONSTER).setTitle(name);
            } else {
                loot_table_set = register(name, loot_table_key).addTags(BibTags.ENTITY).setTitle(name);
            }

            loot_table_set.setIcon(with_item_icon.getItemIcon());
        });

        flushBuffer("all_entities", "All Entities").addTags(BibTags.ENTITY).setIcon(Items.CHEST);
        register("all_monsters", monsters).setTitle("All monsters").addTags(BibTags.ENTITY).setIcon(Items.CHEST);
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
