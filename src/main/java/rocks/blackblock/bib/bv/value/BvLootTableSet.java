package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;
import rocks.blackblock.bib.util.BibServer;
import rocks.blackblock.bib.util.BibText;

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

    }

    /**
     * Serialize this value to NBT
     *
     * @since 0.2.0
     */
    @Override
    public @Nullable NbtElement toNbt() {
        return null;
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
