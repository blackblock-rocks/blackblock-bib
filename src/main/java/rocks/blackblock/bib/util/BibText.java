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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.BibMod;

import java.io.StringReader;
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
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibText() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
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

        while (index > -1 && Character.isDigit(slug.charAt(index))) {
            index--;
        }

        if (index == slug.length() - 1) {
            return slug + "-2";
        }

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
}
