package rocks.blackblock.bib.serialize;

import com.google.gson.JsonElement;

/**
 * Indicate the current class can be converted into a JSON element
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public interface JSONizable {

    /**
     * Turn this value into a JSON element
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    JsonElement toJson();

}