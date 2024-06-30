package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
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
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class BvMap
    implements
        Map<String, BvElement>,
        BibLog.Argable,
        BvElement<BvMap, BvMap> {

    public static final String TYPE = "map";

    protected Map<String, BvElement> values = new HashMap<>();

    /**
     * Get the identifier of this type
     *
     * @since    0.1.0
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * BvMaps contain themselves
     *
     * @since    0.1.0
     */
    @Override
    public BvMap getContainedValue() {
        return this;
    }

    /**
     * It shouldn't be possible to set the contained value
     *
     * @since    0.1.0
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
     * See if both maps are equal
     *
     * @since    0.1.0
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
     * @since    0.1.0
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
     * @since    0.1.0
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

    @Override
    public void loadFromJson(JsonElement json) {

    }

    @Override
    public @Nullable JsonElement toJson() {
        return null;
    }

    /**
     * Convert to a string for use in commands
     *
     * @since    0.1.0
     */
    @NotNull
    public String toCommandString() {
        return "map";
    }

    /**
     * Convert to pretty text
     *
     * @since    0.1.0
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
     * @since    0.1.0
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
     * @since    0.1.0
     */
    @Override
    public String toString() {
        return this.toBBLogArg().toString();
    }

    /**
     * Forward all Map methods to the inner map
     *
     * @since    0.1.0
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
