package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.util.BibLog;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A map of BV values
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvMap
    implements
        Map<String, BvElement>,
        BibLog.Argable,
        BvElement<BvMap, BvMap> {

    public static final String TYPE = "map";

    protected Map<String, BvElement> values = new HashMap<>();

    // All the tags this element might have
    protected Set<BvElement> tags = null;

    /**
     * Get the identifier of this type
     *
     * @since    0.2.0
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * BvMaps contain themselves
     *
     * @since    0.2.0
     */
    @Override
    public BvMap getContainedValue() {
        return this;
    }

    /**
     * Set the values
     *
     * @since    0.2.0
     */
    @Override
    public void setContainedValue(BvMap value) {

        if (value == null) {
            this.values.clear();
            return;
        }

        this.values = value.values;
    }

    /**
     * Get all the tags of this element
     *
     * @since    0.2.0
     */
    @Override
    public Set<BvElement> getTags() {
        return this.tags;
    }

    /**
     * Add a tag to this element
     *
     * @since    0.2.0
     */
    @Override
    public void addTag(BvElement tag) {

        if (this.tags == null) {
            this.tags = new HashSet<>();
        }

        this.tags.add(tag);
    }

    /**
     * Remove all the tags of this element
     *
     * @since    0.2.0
     */
    @Override
    public void clearTags() {
        this.tags = null;
    }

    /**
     * See if both maps are equal
     *
     * @since    0.2.0
     */
    @Override
    public boolean equalsOtherValue(BvMap other) {

        if (this == other) {
            return true;
        }

        if (this.values == other.values) {
            return true;
        }

        return false;
    }

    /**
     * Load from the given NBT value
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromNbt(NbtElement nbt_value) {

        this.values.clear();

        if (nbt_value instanceof NbtCompound compound) {

            for (String key : compound.getKeys()) {

                NbtElement element_nbt = compound.get(key);

                BvElement element = BvElement.parseFromNbt(element_nbt);

                if (element != null) {
                    this.values.put(key, element);
                }
            }
        }
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable NbtElement toNbt() {

        NbtCompound result = new NbtCompound();

        Map<String, BvElement> values = this.getContainedValue();

        if (values != null && !values.isEmpty()) {
            values.forEach((key, element) -> {

                NbtCompound element_nbt = BvElement.serializeToNbt(element);

                if (element_nbt == null) {
                    return;
                }

                result.put(key, element_nbt);
            });
        }

        return result;
    }

    /**
     * Serialize this value to JSON
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable JsonElement toJson() {

        JsonObject result = new JsonObject();

        Map<String, BvElement> values = this.getContainedValue();

        if (values != null && !values.isEmpty()) {
            values.forEach((key, element) -> {

                JsonObject element_json = BvElement.serializeToJson(element);

                if (element_json == null) {
                    return;
                }

                result.add(key, element_json);
            });
        }

        return null;
    }

    /**
     * Load the value from JSON
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

        this.values.clear();

        if (json instanceof JsonObject compound) {

            for (String key : compound.keySet()) {

                JsonElement element_json = compound.get(key);
                BvElement element = BvElement.parseFromJson(element_json);

                if (element != null) {
                    this.values.put(key, element);
                }
            }
        }
    }

    /**
     * Convert to a string for use in commands
     *
     * @since    0.2.0
     */
    @NotNull
    public String toCommandString() {
        return "map";
    }

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @Nullable
    @Override
    public Text toPrettyText() {

        Map<String, BvElement> values = this.getContainedValue();

        if (values == null) {
            return null;
        }

        MutableText result = Text.literal("{");

        AtomicInteger count = new AtomicInteger(0);
        values.forEach((key, element) -> {

            if (count.get() > 0) {
                result.append(Text.literal(", "));
            }

            result.append(Text.literal('"' + key + '"' + ": "));
            result.append(BvElement.getPrettyText(element));

            count.addAndGet(1);
        });

        result.append(Text.literal("}"));

        return result;
    }

    /**
     * Return a Arg instance
     *
     * @since    0.2.0
     */
    @Override
    public BibLog.Arg toBBLogArg() {

        BibLog.Arg result = BibLog.createArg(this);
        int size = 0;

        if (this.values != null) {
            size = this.values.size();
        }

        result.add("type", this.getType());
        result.add("values", this.values);
        result.add("size", size);

        return result;
    }

    /**
     * Return a string representation
     *
     * @since    0.2.0
     */
    @Override
    public String toString() {
        return this.toBBLogArg().toString();
    }

    /**
     * Get the string to use in placeholders
     *
     * @since    0.2.0
     */
    @Override
    public String toPlaceholderString() {
        return this.toString();
    }

    /**
     * Forward all Map methods to the inner map
     *
     * @since    0.2.0
     */
    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.values.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.values.containsValue(o);
    }

    @Override
    public BvElement get(Object o) {
        return this.values.get(o);
    }

    @Nullable
    @Override
    public BvElement put(String s, BvElement value) {
        return this.values.put(s, value);
    }

    @Override
    public BvElement remove(Object o) {
        return this.values.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends BvElement> map) {
        this.values.putAll(map);
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return this.values.keySet();
    }

    @NotNull
    @Override
    public Collection<BvElement> values() {
        return this.values.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, BvElement>> entrySet() {
        return this.values.entrySet();
    }
}
