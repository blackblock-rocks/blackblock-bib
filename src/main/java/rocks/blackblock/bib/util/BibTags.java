package rocks.blackblock.bib.util;

import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvTag;

/**
 * A lot of BvElements that can be used as tags
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class BibTags {

    /**
     * Create a tag and optionally tag it with the given tags
     *
     * @since    0.2.0
     */
    public static BvTag create(String name, BvTag... add_tags) {

        StringBuilder tag_id = new StringBuilder(name);

        if (add_tags != null) {
            for (BvTag add_tag : add_tags) {
                tag_id.append("_").append(add_tag.getContainedValue());
            }
        }

        BvTag tag = BvTag.get(tag_id.toString());
        tag.setTitle(BibText.titleize(name));

        if (add_tags != null) {
            for (BvTag add_tag : add_tags) {
                tag.addTag(add_tag);
            }
        }

        return tag;
    }

    // Core tags
    public static final BvTag ENTITY = create("entity");
    public static final BvTag STRUCTURE = create("structure");
    public static final BvTag BLOCK = create("block");
    public static final BvTag ITEM = create("item");
    public static final BvTag BIOME = create("biome");
    public static final BvTag LOOT_CHEST = create("loot_chest");
    public static final BvTag TRAPS = create("traps");
    public static final BvTag EQUIPMENT = create("equipment");
    public static final BvTag FISHING = create("fishing");
    public static final BvTag HUSBANDRY = create("husbandry");
    public static final BvTag ARCHEOLOGY = create("archeology");

    // Structure tags
    public static final BvTag END_CITY = create("end_city", STRUCTURE);
    public static final BvTag NETHER_FORTRESS = create("fortress", STRUCTURE);
    public static final BvTag JUNGLE_TEMPLE = create("jungle_temple", STRUCTURE);
    public static final BvTag DESERT_PYRAMID = create("desert_pyramid", STRUCTURE);
    public static final BvTag IGLOO = create("igloo", STRUCTURE);
    public static final BvTag SWAMP_HUT = create("swamp_hut", STRUCTURE);
    public static final BvTag VILLAGE = create("village", STRUCTURE);
    public static final BvTag STRONGHOLD = create("stronghold", STRUCTURE);
    public static final BvTag MINESHAFT = create("mineshaft", STRUCTURE);
    public static final BvTag OCEAN_RUIN = create("ocean_ruin", STRUCTURE);
    public static final BvTag SHIPWRECK = create("shipwreck", STRUCTURE);
    public static final BvTag BASTION_REMNANT = create("bastion_remnant", STRUCTURE);
    public static final BvTag RUINED_PORTAL = create("ruined_portal", STRUCTURE);
    public static final BvTag END_GATEWAY = create("end_gateway", STRUCTURE);
    public static final BvTag END_PORTAL = create("end_portal", STRUCTURE);
    public static final BvTag NETHER_PORTAL = create("nether_portal", STRUCTURE);
    public static final BvTag TRIAL_CHAMBER = create("trial_chamber", STRUCTURE);
    public static final BvTag WOODLAND_MANSION = create("woodland_mansion", STRUCTURE);
    public static final BvTag PILLAGER_OUTPOST = create("pillager_outpost", STRUCTURE);
    public static final BvTag ANCIENT_CITY = create("ancient_city", STRUCTURE);
    public static final BvTag DUNGEON = create("dungeon", STRUCTURE);

    // Entity types
    public static final BvTag PLAYER = create("player", ENTITY);
    public static final BvTag MONSTER = create("monster", ENTITY);
    public static final BvTag ANIMAL = create("animal", ENTITY);
    public static final BvTag VILLAGER = create("villager", ENTITY);
    public static final BvTag GOLEM = create("golem", ENTITY);
    public static final BvTag BOSS = create("boss", ENTITY);
    public static final BvTag PET = create("pet", ENTITY);
    public static final BvTag NEUTRAL = create("neutral", ENTITY);

}
