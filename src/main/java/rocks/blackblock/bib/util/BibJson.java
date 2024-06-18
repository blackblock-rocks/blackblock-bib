package rocks.blackblock.bib.util;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.serialize.JSONizable;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Library class for working with JSON data
 * and serializing Minecraft instances to JSON
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibJson {

    // The GSON instance to use for serializing
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibJson() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get a string from the given object.
     * If it does not exist, return null.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object   The json object the string should be in
     * @param    key      The key the string should be found under
     */
    @Nullable
    public static String getString(JsonObject object, String key) {
        return getString(object, key, null);
    }

    /**
     * Get a string from the given object.
     * If it does not exist, return the default value.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     * @param    default_value  The default value to return if the key does not exist or is not valid
     */
    public static String getString(JsonObject object, String key, @Nullable String default_value) {

        if (!object.has(key)) {
            return default_value;
        }

        JsonElement element = object.get(key);

        if (element.isJsonNull()) {
            return default_value;
        }

        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return default_value;
    }

    /**
     * Get an integer from the given object.
     * If it does not exist, return null.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     */
    @Nullable
    public static Integer getInteger(JsonObject object, String key) {
        return getInteger(object, key, null);
    }

    /**
     * Get an integer from the given object.
     * If it does not exist, return the default value.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     * @param    default_value  The default value to return if the key does not exist or is not valid
     */
    public static Integer getInteger(JsonObject object, String key, @Nullable Integer default_value) {
        if (object.has(key)) {
            JsonElement element = object.get(key);

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();

                if (primitive.isNumber()) {
                    return primitive.getAsInt();
                }
            }
        }

        return default_value;
    }

    /**
     * Get a boolean from the given object.
     * If it does not exist, return null.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     */
    @Nullable
    public static Boolean getBoolean(JsonObject object, String key) {
        return getBoolean(object, key, null);
    }

    /**
     * Get a boolean from the given object.
     * If it does not exist, return the default value.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     * @param    default_value  The default value to return if the key does not exist or is not valid
     */
    public static Boolean getBoolean(JsonObject object, String key, @Nullable Boolean default_value) {
        if (object.has(key)) {
            JsonElement element = object.get(key);

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();

                if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                }
            }
        }

        return default_value;
    }

    /**
     * Get a double from the given object.
     * If it does not exist, return null.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     */
    @Nullable
    public static Double getDouble(JsonObject object, String key) {
        return getDouble(object, key, null);
    }

    /**
     * Get a boolean from the given object.
     * If it does not exist, return the default value.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     * @param    default_value  The default value to return if the key does not exist or is not valid
     */
    public static Double getDouble(JsonObject object, String key, @Nullable Double default_value) {
        if (object.has(key)) {
            JsonElement element = object.get(key);

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();

                if (primitive.isNumber()) {
                    return primitive.getAsDouble();
                }
            }
        }

        return default_value;
    }

    /**
     * Get a float from the given object.
     * If it does not exist, return null.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     */
    @Nullable
    public static Float getFloat(JsonObject object, String key) {
        return getFloat(object, key, null);
    }

    /**
     * Get a boolean from the given object.
     * If it does not exist, return the default value.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object         The json object the string should be in
     * @param    key            The key the string should be found under
     * @param    default_value  The default value to return if the key does not exist or is not valid
     */
    public static Float getFloat(JsonObject object, String key, @Nullable Float default_value) {
        if (object.has(key)) {
            JsonElement element = object.get(key);

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();

                if (primitive.isNumber()) {
                    return primitive.getAsFloat();
                }
            }
        }

        return default_value;
    }

    /**
     * Get a UUID
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    object   The json object the string should be in
     * @param    key      The key the string should be found under
     */
    @Nullable
    public static UUID getUUID(JsonObject object, String key) {

        String string_value = getString(object, key);

        if (string_value == null || string_value.isEmpty()) {
            return null;
        }

        UUID result = null;

        try {
            result = UUID.fromString(string_value);
        } catch (Exception e) {
            // Ignore
        }

        return result;
    }

    /**
     * Parse from a file
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    file   The file to parse
     */
    @Nullable
    public static JsonElement parse(File file) {

        String json;

        try {
            json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }

        JsonElement result = null;

        try {
            result = GSON.fromJson(json, JsonElement.class);

            if (result == null) {
                return JsonNull.INSTANCE;
            }

        } catch (Exception e) {
            BibLog.log("Failed to parse JSON of file " + file.getAbsolutePath(), e);
        }

        return result;
    }

    /**
     * Revive a string
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    json   The json string to revive
     */
    @Nullable
    public static JsonObject parse(String json) {

        JsonObject result = null;

        try {
            result = GSON.fromJson(json, JsonObject.class);
        } catch (Exception e) {
            BibLog.log("Failed to parse JSON:", e);
        }

        return result;
    }

    /**
     * Turn the given Text into a string
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    value
     */
    @NotNull
    public static String stringify(Text value) {

        if (value == null) {
            value = Text.literal("");
        }

        return BibText.serializeToJson(value).toString();
    }

    /**
     * Turn the given element into a string
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    element   The json element to stringify
     */
    @NotNull
    public static String stringify(JsonElement element) {

        // For fuck's sake Google, all this to just change the default indent
        StringWriter writer = new StringWriter();
        JsonWriter json_writer = new JsonWriter(writer);

        json_writer.setIndent("\t");

        GSON.toJson(element, json_writer);

        return writer.toString();
    }

    /**
     * Jsonify an object
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    obj   The object to jsonify
     */
    @NotNull
    public static JsonElement jsonify(Object obj) {

        if (obj == null) {
            return JsonNull.INSTANCE;
        }

        if (obj instanceof JsonElement element) {
            return element;
        }

        if (obj instanceof JSONizable json_izable) {
            return json_izable.toJson();
        }

        if (obj instanceof String str) {
            return new JsonPrimitive(str);
        }

        if (obj instanceof Number nr) {
            return new JsonPrimitive(nr);
        }

        if (obj instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }

        if (obj instanceof List list) {
            JsonArray json_array = new JsonArray();

            for (Object entry : list) {
                json_array.add(jsonify(entry));
            }

            return json_array;
        }

        if (obj instanceof ItemStack stack) {
            return jsonify(stack);
        }

        if (obj instanceof Inventory inventory) {
            return jsonify(inventory);
        }

        // Try any `toJson` or `toJSON` method
        try {
            Method toJson = null;

            try {
                toJson = obj.getClass().getMethod("toJson");
            } catch (NoSuchMethodException e) {}

            if (toJson == null) {
                try {
                    toJson = obj.getClass().getMethod("toJSON");
                } catch (NoSuchMethodException e) {}
            }

            if (toJson != null) {
                Object invoked_result = toJson.invoke(obj);

                if (invoked_result instanceof JsonElement element) {
                    return element;
                }

                if (invoked_result instanceof String str) {
                    JsonElement parsed = BibJson.parse(str);

                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore!
        }

        String str = obj.toString();
        JsonObject fallback = new JsonObject();
        fallback.addProperty("class", obj.getClass().getSimpleName());
        fallback.addProperty("string", str);

        return fallback;
    }

    /**
     * Jsonify an Entity
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    entity   The entity to jsonify
     */
    @NotNull
    public static JsonObject jsonify(Entity entity) {

        JsonObject entity_data = new JsonObject();
        entity_data.addProperty("type", "entity");
        entity_data.addProperty("uuid", entity.getUuidAsString());
        entity_data.addProperty("entity_type", entity.getClass().getSimpleName());
        entity_data.add("block_pos", jsonify(entity.getBlockPos()));
        entity_data.addProperty("world", entity.getWorld().getRegistryKey().toString());

        if (entity instanceof LivingEntity living) {
            entity_data.addProperty("health", living.getHealth());
        }

        return entity_data;
    }

    /**
     * Jsonify a BlockPos
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    block_pos   The blockpos to jsonify
     */
    @NotNull
    public static JsonObject jsonify(@NotNull BlockPos block_pos) {

        JsonObject data = new JsonObject();
        data.addProperty("type", "block_pos");
        data.addProperty("x", block_pos.getX());
        data.addProperty("y", block_pos.getY());
        data.addProperty("z", block_pos.getZ());

        return data;
    }

    /**
     * Jsonify an Inventory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    inventory   The inventory to jsonify
     */
    @NotNull
    public static JsonObject jsonify(Inventory inventory) {

        JsonObject result = new JsonObject();
        result.addProperty("type", "inventory");

        if (inventory == null) {
            return result;
        }

        JsonArray items = new JsonArray();

        result.addProperty("size", inventory.size());

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty()) {
                items.add(JsonNull.INSTANCE);
            } else {
                items.add(jsonify(stack));
            }
        }

        result.add("items", items);

        return result;
    }

    /**
     * Jsonify an ItemStack
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    stack   The stack to jsonify
     */
    @NotNull
    public static JsonObject jsonify(ItemStack stack) {

        JsonObject result = new JsonObject();

        Item item = stack.getItem();
        Identifier id = Registries.ITEM.getId(item);

        String item_name = BibText.toTranslatedString(stack.getName());

        result.add("id", jsonify(id));
        result.addProperty("item", stack.getItem().toString());
        result.addProperty("count", stack.getCount());
        result.addProperty("name", item_name);

        List<Text> tooltips = new ArrayList<>(0);

        try {
            List<Text> all_tooltips = stack.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC);

            for (Text tooltip : all_tooltips) {
                String text = BibText.toTranslatedString(tooltip);

                if (text == null) {
                    continue;
                }

                // Skip adding the item name again
                if (text.equals(item_name)) {
                    continue;
                }

                tooltips.add(tooltip);
            }

        } catch (Exception | NoClassDefFoundError ignored) {
            // Ignore
        }

        try {
            if (!tooltips.isEmpty()) {
                List<String> string_tooltips = new ArrayList<>(tooltips.size());

                for (Text tooltip : tooltips) {
                    string_tooltips.add(BibText.toTranslatedString(tooltip));
                }

                result.add("tooltips", jsonify(string_tooltips));
            }
        } catch (Throwable ignored) {

        }

        String rarity = switch (stack.getRarity()) {
            case COMMON -> "common";
            case UNCOMMON -> "uncommon";
            case RARE -> "rare";
            case EPIC -> "epic";
        };

        result.addProperty("rarity", rarity);

        // @TODO: This was used by external tools to get more info from items
        // A lot of this info has been moved to components,
        // so those should be serialized too!
        NbtCompound nbt = BibItem.getCustomNbt(stack);
        if (nbt != null && !nbt.isEmpty()) {
            result.add("nbt", jsonify(nbt));
        }

        return result;
    }

    /**
     * Jsonify an identifier
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    id   The identifier to jsonify
     */
    @NotNull
    public static JsonObject jsonify(Identifier id) {

        JsonObject result = new JsonObject();

        result.addProperty("type", "identifier");
        result.addProperty("namespace", id.getNamespace());
        result.addProperty("path", id.getPath());

        return result;
    }

    /**
     * Jsonify NBT data
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    nbt   The nbt to jsonify
     */
    @NotNull
    public static JsonElement jsonify(NbtElement nbt) {

        if (nbt == null) {
            return JsonNull.INSTANCE;
        }

        if (nbt instanceof NbtCompound compound) {
            return jsonify(compound);
        }

        if (nbt instanceof NbtList list) {
            return jsonify(list);
        }

        if (nbt instanceof NbtString str) {
            return new JsonPrimitive(str.asString());
        }

        if (nbt instanceof NbtByte byte_val) {
            return new JsonPrimitive(byte_val.byteValue());
        }

        if (nbt instanceof NbtShort short_val) {
            return new JsonPrimitive(short_val.shortValue());
        }

        if (nbt instanceof NbtInt nbt_val) {
            return new JsonPrimitive(nbt_val.intValue());
        }

        if (nbt instanceof NbtLong nbt_val) {
            return new JsonPrimitive(nbt_val.longValue());
        }

        if (nbt instanceof NbtFloat nbt_val) {
            return new JsonPrimitive(nbt_val.floatValue());
        }

        if (nbt instanceof NbtDouble nbt_val) {
            return new JsonPrimitive(nbt_val.doubleValue());
        }

        if (nbt instanceof NbtByteArray nbt_val) {
            JsonArray numbers = new JsonArray();

            for (byte b : nbt_val.getByteArray()) {
                numbers.add((int) b);
            }

            return numbers;
        }

        if (nbt instanceof NbtIntArray nbt_val) {
            JsonArray numbers = new JsonArray();

            for (int val : nbt_val.getIntArray()) {
                numbers.add(val);
            }

            return numbers;
        }

        if (nbt instanceof NbtLongArray nbt_val) {
            JsonArray numbers = new JsonArray();

            for (long val : nbt_val.getLongArray()) {
                numbers.add(val);
            }

            return numbers;
        }

        return JsonNull.INSTANCE;
    }

    /**
     * Jsonify an NBT compound
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    nbt   The nbt to jsonify
     */
    @NotNull
    private static JsonObject jsonify(NbtCompound nbt) {

        JsonObject result = new JsonObject();

        for (String key : nbt.getKeys()) {
            result.add(key, jsonify(nbt.get(key)));
        }

        return result;
    }

    /**
     * Jsonify an NBT list
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    nbt   The nbt to jsonify
     */
    @NotNull
    private static JsonArray jsonify(NbtList nbt) {

        JsonArray result = new JsonArray();

        for (NbtElement element : nbt) {
            result.add(jsonify(element));
        }

        return result;
    }
}
