package rocks.blackblock.bib.bv.parameter;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvInteger;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.bv.value.BvString;
import rocks.blackblock.bib.command.CommandLeaf;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum parameter
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class EnumParameter<V extends BvElement<?, ?>>
    extends
        TweakParameter<V> {

    /**
     * Initialize the parameter
     *
     * @since 0.2.0
     */
    protected EnumParameter(String name) {
        super(name);
    }

    /**
     * Create an enum parameter with integer keys
     *
     * @since 0.2.0
     */
    public static <V extends BvElement<?, ?>> EnumParameter<V> withIntegerKeys(String name, Map<Integer, V> values) {
        return new EnumParameter.IntegerKeys<>(name, values);
    }

    /**
     * Create an enum parameter with string keys
     *
     * @since 0.2.0
     */
    public static <V extends BvElement<?, ?>> EnumParameter<V> withStringKeys(String name, Map<String, V> values) {
        return new EnumParameter.StringKeys<>(name, values);
    }

    /**
     * Return the map with the values
     *
     * @since 0.2.0
     */
    abstract protected Map<?, V> getAllValues();

    /**
     * Return the class of the contained type
     *
     * @since 0.2.0
     */
    @Override
    public Class<V> getContainedTypeClass() {
        return null;
    }

    /**
     * Cast the given value
     *
     * @since    0.2.0
     */
    @Nullable
    public V castToContainedType(BvElement input) {

        if (input == null) {
            return null;
        }

        if (this.getAllValues().containsValue(input)) {
            return (V) input;
        }

        return null;
    }

    /**
     * Find the value entered via a command string
     *
     * @since    0.2.0
     */
    @Nullable
    public V findValueFromCommandString(String command_string_identifier) {

        if (command_string_identifier == null || command_string_identifier.isBlank()) {
            return null;
        }

        for (V val : this.getAllValues().values()) {

            if (command_string_identifier.equals(val.toCommandString())) {
                return val;
            }
        }

        return null;
    }

    /**
     * Configure the "set" part of the commands
     *
     * @since    0.2.0
     */
    public CommandLeaf configureSetLeaf(CommandLeaf set_leaf) {

        CommandLeaf value_leaf = set_leaf.getChild("value");
        value_leaf.setType(StringArgumentType.string());

        value_leaf.suggests((context, builder) -> {

            this.getAllValues().forEach((o, v) -> {

                String str = v.toCommandString();
                Message tooltip = v.toCommandTooltip();

                if (str == null) {
                    return;
                }

                if (tooltip == null) {
                    builder.suggest(str);
                } else {
                    builder.suggest(str, tooltip);
                }
            });

            return builder.buildFuture();
        });

        value_leaf.onExecute(command_context -> {
            String value = StringArgumentType.getString(command_context, "value");

            BvMap data_context = this.getDataContext(command_context);
            var succeeded = this.setInRootMap(data_context, this.findValueFromCommandString(value));

            if (succeeded) {
                return 1;
            }

            command_context.getSource().sendError(Text.literal("Failed to set value"));

            return 0;
        });

        return value_leaf;
    }

    /**
     * The enum class that stores values with integer keys
     *
     * @since    0.2.0
     */
    public static class IntegerKeys<V extends BvElement<?, ?>> extends EnumParameter<V> {

        protected Map<Integer, V> int_to_val;
        protected Map<V, BvInteger> val_to_bvint;

        /**
         * Initialize the parameter
         *
         * @since 0.2.0
         */
        protected IntegerKeys(String name, Map<Integer, V> values) {
            super(name);

            if (values == null) {
                values = Map.of();
            }

            this.int_to_val = values;
            this.val_to_bvint = new HashMap<>(values.size());

            values.forEach((integer, v) -> {
                BvInteger key = new BvInteger();
                key.setContainedValue(integer);
                this.val_to_bvint.put(v, key);
            });
        }

        /**
         * Return the map with the values
         *
         * @since 0.2.0
         */
        protected Map<?, V> getAllValues() {
            return this.int_to_val;
        }

        /**
         * Cast the value before setting it in the data context map
         *
         * @since    0.2.0
         */
        @Override
        @Nullable
        public BvInteger castForDataContextSet(V value) {
            BvInteger key = this.val_to_bvint.get(value);
            return key;
        }

        /**
         * Cast the value from the data context map
         *
         * @since    0.2.0
         */
        @Nullable
        public V castFromDataContext(BvElement value) {

            if (!(value instanceof BvInteger bv_int_val)) {
                return null;
            }

            Integer int_key = bv_int_val.getContainedValue();

            return this.int_to_val.get(int_key);
        }
    }

    /**
     * The enum class that stores values with string keys
     *
     * @since    0.2.0
     */
    public static class StringKeys<V extends BvElement<?, ?>> extends EnumParameter<V> {

        protected Map<String, V> string_to_val;
        protected Map<V, BvString> val_to_bvstring;

        /**
         * Initialize the parameter
         *
         * @since 0.2.0
         */
        protected StringKeys(String name, Map<String, V> values) {
            super(name);

            if (values == null) {
                values = Map.of();
            }

            this.string_to_val = values;

            this.val_to_bvstring = new HashMap<>(values.size());

            values.forEach((str, v) -> {
                BvString key = new BvString();
                key.setContainedValue(str);
                this.val_to_bvstring.put(v, key);
            });
        }

        /**
         * Return the map with the values
         *
         * @since 0.2.0
         */
        protected Map<?, V> getAllValues() {
            return this.string_to_val;
        }

        /**
         * Cast the value before setting it in the data context map
         *
         * @since    0.2.0
         */
        @Override
        @Nullable
        public BvString castForDataContextSet(V value) {
            BvString key = this.val_to_bvstring.get(value);
            return key;
        }

        /**
         * Cast the value from the data context map
         *
         * @since    0.2.0
         */
        @Nullable
        public V castFromDataContext(BvElement value) {

            if (!(value instanceof BvString bv_string_val)) {
                return null;
            }

            String string_key = bv_string_val.getContainedValue();

            return this.string_to_val.get(string_key);
        }
    }

}
