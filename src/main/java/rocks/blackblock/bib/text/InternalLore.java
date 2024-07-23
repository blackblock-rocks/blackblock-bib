package rocks.blackblock.bib.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rocks.blackblock.bib.bv.value.BvElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder of lore
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class InternalLore<T extends InternalLore<?>> {

    protected List<Text> lines = new ArrayList<>();

    /**
     * Add the given data
     * @since    0.2.0
     */
    public T add(String text) {

        // Split the text into lines
        String[] parts = text.split("\n");

        for (String part : parts) {
            this.addLine(part);
        }

        return (T) this;
    }

    /**
     * Add the given data
     * @since    0.2.0
     */
    public T add(List<? extends Text> text) {

        if (text != null) {
            this.lines.addAll(text);
        }

        return (T) this;
    }

    /**
     * Add a line
     * @since    0.2.0
     */
    public T addLine(Object value) {

        if (value == null) {
            this.lines.add(Text.literal(""));
            return (T) this;
        }

        Text value_text;

        if (value instanceof Text text) {
            value_text = text;
        } else if (value instanceof BvElement<?, ?> bv_element) {
            value_text = bv_element.toPrettyText();
        } else {
            value_text = Text.literal(value.toString());
        }

        this.lines.add(value_text);
        return (T) this;
    }

    /**
     * Add a key-val line
     * @since    0.2.0
     */
    public T addLine(String key, Object value) {

        if (key == null && value == null) {
            this.lines.add(Text.literal(""));
            return (T) this;
        }

        MutableText key_text = Text.literal(key).formatted(Formatting.GRAY);
        Text value_text;

        if (value instanceof Text text) {
            value_text = text;
        } else if (value instanceof BvElement<?, ?> bv_element) {
            value_text = bv_element.toPrettyText();
        } else {
            if (value == null) {
                value_text = Text.literal("null").formatted(Formatting.GRAY);
            } else {
                value_text = Text.literal(value.toString()).formatted(Formatting.AQUA);
            }
        }

        this.lines.add(key_text.append(": ").append(value_text));
        return (T) this;
    }

    /**
     * Get the lines
     * @since    0.2.0
     */
    public List<Text> getLines() {
        return this.lines;
    }
}
