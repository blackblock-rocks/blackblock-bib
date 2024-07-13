package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.*;

import java.util.*;

/**
 * A BV wrapped set of loot table
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvLootTableSet extends AbstractBvType<Set<LootTable>, BvLootTableSet> {

    public static final String TYPE = "loot_table_set";

    private Set<RegistryKey<LootTable>> keys;
    private String title = null;
    private String generated_title = null;
    private Item icon = null;
    private String ref_id = null;

    /**
     * Revive from the given NBT element if possible
     *
     * @since 0.2.0
     */
    public static BvLootTableSet fromNbt(NbtElement nbt_value) {

        if (!(nbt_value instanceof NbtCompound data)) {
            return null;
        }

        if (!data.contains("ref_id", NbtElement.STRING_TYPE)) {
            return null;
        }

        String ref_id = data.getString("ref_id");

        return BibLoot.LOOT_TABLES_BY_ID.get(ref_id);
    }

    /**
     * Create with a single registry key
     *
     * @since 0.2.0
     */
    public BvLootTableSet() {
        this.keys = new HashSet<>();
    }

    /**
     * Create with a single registry key
     *
     * @since 0.2.0
     */
    public BvLootTableSet(RegistryKey<LootTable> key) {
        this.keys = Set.of(key);
    }

    /**
     * Create with a set of registry keys
     *
     * @since 0.2.0
     */
    public BvLootTableSet(Set<RegistryKey<LootTable>> keys) {
        this.keys = keys;
    }

    /**
     * Set the reference ID
     *
     * @since   0.5.0
     */
    public void setRefId(String id) {
        this.ref_id = id;
    }

    /**
     * Is the given ID part of this set?
     *
     * @since   0.5.0
     */
    public boolean contains(Identifier id) {

        if (this.keys == null) {
            return false;
        }

        for (RegistryKey<LootTable> key : this.keys) {
            if (key.getValue().equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add multiple tags to this element
     *
     * @since    0.2.0
     */
    public BvLootTableSet addTags(BvTag... tags) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }

        this.tags.addAll(Arrays.asList(tags));

        return this;
    }

    /**
     * Set a combined title
     *
     * @since 0.2.0
     */
    public BvLootTableSet setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the title of this value (for visual stuff)
     *
     * @since    0.2.0
     */
    @Override
    public String getDisplayTitle() {

        if (this.title != null) {
            return this.title;
        }

        if (this.generated_title != null) {
            return this.generated_title;
        }

        StringBuilder result = new StringBuilder();

        if (this.keys != null) {
            var count = 0;

            for (var key : this.keys) {
                var path = key.getValue().getPath();
                var last_name = BibText.getAfterLast(path, "/");
                var title = BibText.titleize(last_name);

                if (title != null && !title.isBlank()) {
                    if (count > 0) result.append(", ");
                    result.append(title);
                    count++;
                }
            }
        }

        this.generated_title = result.toString();

        return this.generated_title;
    }

    /**
     * Get the lore to use
     *
     * @since 0.2.0
     */
    @Override
    @Nullable
    public List<Text> getDisplayDescription() {

        if (this.keys == null) {
            return null;
        }

        List<Text> result = new ArrayList<>(this.keys.size());

        for (var key : this.keys) {
            result.add(Text.literal(" - ").append(Text.literal(key.getValue().getPath()).formatted(Formatting.AQUA)));
        }

        return result;
    }

    /**
     * Set the icon to use
     *
     * @since 0.2.0
     */
    public BvLootTableSet setIcon(Item icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Get the icon to use
     *
     * @since 0.2.0
     */
    @Override
    public Item getItemIcon() {
        return this.icon;
    }

    /**
     * Get the identifier of this type
     *
     * @since 0.2.0
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Get the contained set
     *
     * @since 0.2.0
     */
    @Override
    public Set<LootTable> getContainedValue() {

        Set<LootTable> result = new HashSet<>();

        var registry = BibServer.getServer().getReloadableRegistries();

        for (RegistryKey<LootTable> key : this.keys) {
            var table = registry.getLootTable(key);

            if (table == null) {
                BibLog.log("Failed to find loot table", key);
                continue;
            }

            result.add(table);
        }

        return result;
    }

    /**
     * Load this value from NBT
     *
     * @param nbt_value
     * @since 0.2.0
     */
    @Override
    public void loadFromNbt(NbtElement nbt_value) {

        if (!(nbt_value instanceof NbtCompound data)) {
            return;
        }

        if (data.contains("ref_id", NbtElement.STRING_TYPE)) {
            this.ref_id = data.getString("ref_id");

            if (BibLoot.LOOT_TABLES_BY_ID.containsKey(this.ref_id)) {
                var other_instance = BibLoot.LOOT_TABLES_BY_ID.get(this.ref_id);

                if (other_instance != null) {
                    this.keys = other_instance.keys;
                    this.title = other_instance.title;
                    this.generated_title = other_instance.generated_title;
                    this.icon = other_instance.icon;
                }

                return;
            }
        }

        if (data.contains("keys", NbtElement.LIST_TYPE)) {
            NbtList list = data.getList("keys", NbtElement.COMPOUND_TYPE);

            this.keys = new HashSet<>(list.size());

            for (NbtElement element : list) {
                if (!(element instanceof NbtCompound compound)) {
                    continue;
                }

                Identifier identifier = BibData.parseToIdentifier(compound);

                if (identifier == null) {
                    continue;
                }

                RegistryKey<LootTable> id_key = RegistryKey.of(RegistryKeys.LOOT_TABLE, identifier);

                this.keys.add(id_key);
            }
        }
    }

    /**
     * Serialize this value to NBT
     *
     * @since 0.2.0
     */
    @Override
    public @Nullable NbtElement toNbt() {

        NbtCompound data = new NbtCompound();

        if (this.ref_id != null && !this.ref_id.isBlank()) {
            data.putString("ref_id", this.ref_id);
        }

        if (this.keys != null) {
            NbtList list = new NbtList();

            for (RegistryKey<LootTable> key : this.keys) {
                Identifier identifier = key.getValue();

                if (identifier == null) {
                    continue;
                }

                NbtCompound serialized = BibData.serialize(identifier);
                list.add(serialized);
            }

            data.put("keys", list);
        }

        return data;
    }

    /**
     * Load the value from JSON
     *
     * @param json
     * @since 0.2.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

    }

    /**
     * Convert to pretty text
     *
     * @since 0.2.0
     */
    @Override
    public @Nullable Text toPrettyText() {
        return Text.literal(this.keys+"");
    }

    /**
     * Get a string value to use in a placeholder
     *
     * @since 0.2.0
     */
    @Override
    public String toPlaceholderString() {
        return this.keys + "";
    }

    /**
     * Return a Arg instance
     *
     * @since    0.2.0
     */
    @Override
    public BibLog.Arg toBBLogArg() {

        BibLog.Arg result = BibLog.createArg(this);

        result.add("type", this.getType());

        var value = this.keys;

        if (value != null) {
            result.add("size", value.size());
            var values = value.toArray();

            for (int i = 0; i < values.length; i++) {
                result.add("table " + i, values[i]);

                if (i > 5) {
                    break;
                }
            }
        } else {
            result.add("size", 0);
        }

        return result;
    }
}
