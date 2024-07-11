package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;
import rocks.blackblock.bib.util.BibServer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

        var loot_table_registry = BibServer.getDynamicRegistry().get(RegistryKeys.LOOT_TABLE);

        for (RegistryKey<LootTable> key : this.keys) {
            var table = loot_table_registry.get(key);

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
}
