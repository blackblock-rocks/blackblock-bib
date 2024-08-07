package rocks.blackblock.bib.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rocks.blackblock.bib.bv.value.BvElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A builder of lore
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class InternalLore<T extends InternalLore<?>> implements Supplier<Text> {

    protected List<Text> lines = new ArrayList<>();

    /**
     * Add the given data
     * @since    0.2.0
     */
    public T add(String text) {

        // Split the text into lines
        String[] parts = text.split("\n");

        for (String part : parts) {
            this.lines.add(Text.literal(part));
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
            return this.add(value.toString());
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

    /**
     * Turn it into a single text
     * @since    0.2.0
     */
    public MutableText toConcatenatedText() {

        MutableText combined = Text.empty();
        int count = 0;

        for (Text line : this.lines) {
            if (count > 0) {
                combined.append("\n");
            }

            combined.append(line);
            count++;
        }

        return combined;
    }

    /**
     * Get the text
     * @since    0.2.0
     */
    @Override
    public Text get() {
        return this.toConcatenatedText();
    }
}
