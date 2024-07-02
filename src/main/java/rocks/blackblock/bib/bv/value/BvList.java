package rocks.blackblock.bib.bv.value;

import carpet.script.value.ListValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.bv.operator.BvOperator;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.util.BibLog;

import java.util.*;

/**
 * A list of BV values
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BvList<ListContentType extends BvElement>
    implements
        List<ListContentType>,
        BibLog.Argable,
        BvElement<BvList<ListContentType>, BvList<ListContentType>> {

    public static final String TYPE = "list";

    // The actual list
    private List<ListContentType> contents = new ArrayList<>();

    // The (optional) content type
    private String content_type_name = null;

    /**
     * Get a list of the given type
     *
     * @since    0.2.0
     */
    protected static <EntryType extends BvElement> BvList<EntryType> of(List<EntryType> source) {

        BvList<EntryType> result = new BvList<>();

        result.addAll(source);

        return result;
    }

    /**
     * Get the identifier of this type
     *
     * @since    0.2.0
     */
    public String getType() {
        return TYPE;
    }

    /**
     * BvLists contain themselves
     *
     * @since    0.2.0
     */
    @Override
    public BvList<ListContentType> getContainedValue() {
        return this;
    }

    /**
     * Set the values
     *
     * @since    0.2.0
     */
    @Override
    public void setContainedValue(BvList<ListContentType> value) {
        if (value == null) {
            this.contents.clear();
            return;
        }

        this.contents = value.contents;
    }

    /**
     * See if both lists are equal
     *
     * @since    0.2.0
     */
    @Override
    public boolean equalsOtherValue(BvList<ListContentType> other) {
        if (this == other) {
            return true;
        }

        if (this.contents == other.contents) {
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

        this.contents.clear();

        if (nbt_value instanceof NbtList list) {

            for (NbtElement element_nbt : list) {

                BvElement element = BvElement.parseFromNbt(element_nbt);

                if (element == null) {
                    this.contents.add(null);
                } else {
                    try {
                        this.contents.add((ListContentType) element);
                    } catch (Throwable e) {
                        GlitchGuru.registerThrowable(e);
                    }
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

        NbtList result = new NbtList();

        List<ListContentType> values = this.getContainedValue();

        if (values != null && !values.isEmpty()) {
            for (ListContentType entry : values) {
                NbtCompound element_nbt = BvElement.serializeToNbt(entry);
                result.add(element_nbt);
            }
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

        JsonArray result = new JsonArray();

        List<ListContentType> values = this.getContainedValue();

        if (values != null && !values.isEmpty()) {
            for (ListContentType entry : values) {

                if (entry == null) {
                    result.add(JsonNull.INSTANCE);
                    continue;
                }

                JsonObject element_json = BvElement.serializeToJson(entry);

                if (element_json == null) {
                    result.add(JsonNull.INSTANCE);
                    continue;
                }

                result.add(element_json);
            }
        }

        return result;
    }

    /**
     * Load the value from JSON
     *
     * @since    0.2.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

        this.contents.clear();

        if (json instanceof JsonArray json_array) {

            for (JsonElement entry : json_array) {

                BvElement element = BvElement.parseFromJson(entry);

                if (element == null) {
                    this.contents.add(null);
                } else {
                    try {
                        this.contents.add((ListContentType) element);
                    } catch (Throwable e) {
                        GlitchGuru.registerThrowable(e);
                    }
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
        return "list";
    }

    /**
     * Convert to pretty text
     *
     * @since    0.2.0
     */
    @Override
    public @Nullable Text toPrettyText() {

        MutableText result = Text.literal("[");

        int count = 0;
        for (ListContentType entry : this.getContainedValue()) {

            if (count > 0) {
                result.append(Text.literal(", "));
            }

            result.append(BvElement.getPrettyText(entry));
            count++;
        }

        result.append(Text.literal("]"));

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
        List<ListContentType> contents = this.getContainedValue();

        if (contents != null) {
            size = contents.size();
        }

        result.add("type", this.getType());
        result.add("contents", contents);
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
     * Forward all List methods to the inner list
     *
     * @since    0.2.0
     */
    @Override
    public int size() {
        return this.contents.size();
    }

    @Override
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.contents.contains(o);
    }

    @NotNull
    @Override
    public Iterator<ListContentType> iterator() {
        return this.contents.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.contents.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return this.contents.toArray(ts);
    }

    @Override
    public boolean add(ListContentType listContentType) {
        return this.contents.add(listContentType);
    }

    @Override
    public boolean remove(Object o) {
        return this.contents.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return this.contents.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ListContentType> collection) {
        return this.contents.addAll(collection);
    }

    @Override
    public boolean addAll(int i, @NotNull Collection<? extends ListContentType> collection) {
        return this.contents.addAll(i, collection);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return this.contents.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return this.contents.retainAll(collection);
    }

    @Override
    public void clear() {
        this.contents.clear();
    }

    @Override
    public ListContentType get(int i) {
        return this.contents.get(i);
    }

    @Override
    public ListContentType set(int i, ListContentType value) {
        return this.contents.set(i, value);
    }

    @Override
    public void add(int i, ListContentType value) {
        this.contents.add(i, value);
    }

    @Override
    public ListContentType remove(int i) {
        return this.contents.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return this.contents.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.contents.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<ListContentType> listIterator() {
        return this.contents.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<ListContentType> listIterator(int i) {
        return this.contents.listIterator(i);
    }

    @NotNull
    @Override
    public List<ListContentType> subList(int i, int i1) {
        return this.contents.subList(i, i1);
    }

    /**
     * Register all the BvList operators
     *
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static void registerOperators() {

        // Add a value
        new BvOperator<>(BvList.class, "add", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            BibLog.log("Should add", right, "to the list of", left);

            return null;
        });

        // Check if the list is empty
        new BvOperator<>(BvList.class, "is_empty", BvOperator.Type.LOGICAL, (left) -> left == null || left.isEmpty());
    }
}
