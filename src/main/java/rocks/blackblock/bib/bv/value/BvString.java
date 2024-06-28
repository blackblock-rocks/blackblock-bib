package rocks.blackblock.bib.bv.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A BV String
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class BvString extends AbstractBvType<String, BvString> {

    private static final String TYPE = "string";
    private static final Supplier<BvString> SUPPLIER = BvElement.registerType(TYPE, BvString::new);

    /**
     * Create an instance of the given value
     *
     * @since    0.1.0
     */
    public static BvString of(String value) {
        var result = new BvString();
        result.setContainedValue(value);
        return result;
    }

    /**
     * Get the identifier of this type
     *
     * @since    0.1.0
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Load from the given NBT value
     *
     * @since    0.1.0
     */
    @Override
    public void loadFromNbt(NbtElement nbt_value) {

        if (nbt_value == null) {
            this.setContainedValue(null);
            return;
        }

        this.setContainedValue(nbt_value.asString());
    }

    /**
     * Turn this into an NBT element
     *
     * @since    0.1.0
     */
    @Override
    public @Nullable NbtString toNbt() {

        String contained_value = this.getContainedValue();

        if (contained_value == null) {
            return null;
        }

        return NbtString.of(contained_value);
    }

    /**
     * Load from the given JSON value
     *
     * @since    0.1.0
     */
    @Override
    public void loadFromJson(JsonElement json) {

        if (json == null) {
            this.setContainedValue(null);
            return;
        }

        if (!(json instanceof JsonPrimitive json_prim)) {
            throw new RuntimeException("Expected a JsonPrimitive, but got " + json.getClass().getSimpleName());
        }

        this.setContainedValue(json_prim.getAsString());
    }

    /**
     * Convert to pretty text
     *
     * @since    0.1.0
     */
    @NotNull
    @Override
    public Text toPrettyText() {

        String value = this.getContainedValue();

        if (value == null) {
            return null;
        }

        return Text.literal('"' + value + '"').formatted(Formatting.AQUA);
    }
}
