package rocks.blackblock.bib.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.text.InternalLore;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Library class for working with strings and Texts
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibText {

    /**
     * The Lore class
     * @since    0.1.0
     */
    public static class Lore extends InternalLore<Lore> {
        /**
         * Create a new empty instance
         * @since    0.2.0
         */
        protected Lore createEmptyLore() {
            return new Lore();
        }
    }

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibText() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Create a new Lore instance
     * @since    0.2.0
     */
    public static Lore createLore() {
        return new Lore();
    }

    /**
     * Create a new Lore instance of the given info
     * @since    0.2.0
     */
    public static Lore createLore(String lore) {
        var result = new Lore();
        result.add(lore);
        return result;
    }

    /**
     * Create a new Lore instance of the given info
     * @since    0.2.0
     */
    public static Lore createLore(List<? extends Text> lore) {
        var result = new Lore();
        result.add(lore);
        return result;
    }

    /**
     * Remove invalid characters from a string.
     * Used to be a function in SharedConstants
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String stripInvalidChars(String input) {
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            input = input.replace(c, '_');
        }

        return input;
    }

    /**
     * Return the json string representation of the given text
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static JsonElement serializeToJson(Text text) {
        RegistryWrapper.WrapperLookup registries = BibMod.getDynamicRegistry();
        return TextCodecs.CODEC.encodeStart(registries.getOps(JsonOps.INSTANCE), text).getOrThrow(JsonParseException::new);
    }

    /**
     * Parse the given JSON element
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static MutableText deserializeFromJson(String json) {
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        jsonReader.setLenient(true);
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        if (jsonElement == null) {
            return null;
        }

        return BibText.deserializeFromJson(jsonElement);
    }

    /**
     * Parse the given JSON element
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static MutableText deserializeFromJson(@Nullable JsonElement json) {
        return Text.Serialization.fromJsonTree(json, BibMod.getDynamicRegistry());
    }

    /**
     * Get everything after the last string
     *
     * @since    0.2.0
     */
    @Contract("null -> null; !null -> !null")
    public static String getAfterLast(String input, String needle) {

        if (input == null) {
            return null;
        }

        if (needle == null || needle.isBlank()) {
            return input;
        }

        int last_index = input.lastIndexOf(needle);

        if (last_index == -1) {
            return input;
        }

        return input.substring(last_index + 1);
    }

    /**
     * Titleize a string
     *
     * @since    0.2.0
     */
    @Contract("null -> null; !null -> !null")
    public static String titleize(String input) {

        if (input == null) {
            return null;
        }

        if (input.length() > 64) {
            return input;
        }

        // Replace underscores with spaces
        String spaced = input.replace('_', ' ');

        // Insert spaces before each uppercase letter (except the first one)
        spaced = spaced.replaceAll("([a-z])([A-Z])", "$1 $2");

        String titleized = WordUtils.capitalizeFully(spaced);

        return titleized;
    }

    /**
     * Generate a slug from the given string.
     * Check the given map for existing slugs and increment them if needed
     *
     * @since    0.2.0
     */
    @Contract("null -> null; !null -> !null")
    public static String slugify(String text, Collection<String> existing_slugs) {

        if (text == null) {
            return null;
        }

        String slug = slugify(text);

        if (existing_slugs == null) {
            return slug;
        }

        int index = 2;
        String original = slug;

        while (existing_slugs.contains(slug)) {
            String incremented = incrementSlug(slug);

            if (incremented.equals(original)) {
                slug = slug + "-" + index;
                index++;
            } else {
                slug = incremented;
            }
        }

        return slug;
    }

    /**
     * Generate a slug from the given text
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null; !null -> !null")
    public static String slugify(String text) {

        if (text == null) {
            return null;
        }

        text = text.toLowerCase();
        StringBuilder result = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                result.append(c);
            } else {
                result.append('-');
            }
        }

        int index;
        while ((index = result.indexOf("--")) > -1) {
            result.replace(index, index + 2, "-");
        }

        while (!result.isEmpty() && result.charAt(0) == '-') {
            result.deleteCharAt(0);
        }

        while (!result.isEmpty() && result.charAt(result.length() - 1) == '-') {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * Increase the number of the given slug.
     * If the slug ends with a number, increase that number.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null; !null -> !null")
    public static String incrementSlug(String slug) {

        if (slug == null) {
            return null;
        }

        int index = slug.length() - 1;

        // Find the last digit
        while (index > -1 && Character.isDigit(slug.charAt(index))) {
            index--;
        }

        // If there are no digits, add a -2
        if (index == slug.length() - 1) {
            return slug + "-2";
        }

        // Get the number
        String number = slug.substring(index + 1);
        int num = Integer.parseInt(number);

        return slug.substring(0, index + 1) + (num + 1);
    }

    /**
     * Get the actual text's string contents
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null; !null -> !null")
    public static String toString(Text text) {

        if (text == null) {
            return null;
        }

        return text.getString();
    }

    /**
     * Get the string contents of the given Text,
     * and make sure it's translated
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Contract("null -> null; !null -> !null")
    public static String toTranslatedString(Text text) {
        return toString(text);
    }

    /**
     * Fill in placeholders
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String fillPlaceholders(String haystack, Map<String, String> values) {
        return StringSubstitutor.replace(haystack, values, "{", "}");
    }

    /**
     * Add word wrapping
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.2.0
     */
    public static String createWrappedString(String text, int max_width) {

        if (text == null || text.isEmpty() || max_width <= 0) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        String[] words = text.split("\\s+");
        int current_line_length = 0;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            // Handle first word differently
            if (i == 0) {
                result.append(word);
                current_line_length = word.length();
                continue;
            }

            // Check if adding the next word exceeds maxWidth
            if (current_line_length + word.length() + 1 > max_width) {
                result.append("\n").append(word);
                current_line_length = word.length();
            } else {
                result.append(" ").append(word);
                current_line_length += word.length() + 1;
            }
        }

        return result.toString();
    }
}
