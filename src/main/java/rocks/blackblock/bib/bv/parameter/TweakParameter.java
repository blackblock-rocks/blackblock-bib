package rocks.blackblock.bib.bv.parameter;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.tweaks.RootTweakMap;
import rocks.blackblock.bib.tweaks.TweaksConfiguration;
import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.command.CommandLeaf;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main Parameter class
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class TweakParameter<ContainedBvType extends BvElement<?, ?>> implements BibLog.Argable {

    // The name of the parameter
    protected final String name;

    // The optional title of the parameter
    protected String title = null;

    // The default value
    protected ContainedBvType default_value = null;

    // The optional parent parameter (in case we're in a map)
    protected MapParameter<?> parent_parameter = null;

    // The root parameter
    protected MapParameter<?> root_parameter = null;

    // Listeners to call when the value changes
    protected List<Consumer<ChangeContext<ContainedBvType>>> on_change_listeners = new ArrayList<>();

    /**
     * Initialize the parameter
     *
     * @since    0.1.0
     */
    public TweakParameter(String name) {
        this.name = name;
    }

    /**
     * Return the class of the contained type
     *
     * @since    0.1.0
     */
    public abstract Class<? extends BvElement> getContainedTypeClass();

    /**
     * Cast the given value
     *
     * @since    0.1.0
     */
    @Nullable
    public ContainedBvType castToContainedType(BvElement input) {

        if (input == null) {
            return null;
        }

        if (input.getClass() == this.getContainedTypeClass()) {
            return (ContainedBvType) input;
        }

        return null;
    }

    /**
     * Set the default value
     *
     * @since    0.1.0
     */
    public TweakParameter<ContainedBvType> setDefaultValue(ContainedBvType value) {
        this.default_value = value;
        return this;
    }

    /**
     * Get the default value (unsafe, uncloned)
     *
     * @since    0.1.0
     */
    public ContainedBvType getDefaultValue() {
        return this.default_value;
    }

    /**
     * Get the name of this parameter
     *
     * @since    0.1.0
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the parent parameter map
     *
     * @since    0.1.0
     */
    public void setParent(MapParameter<?> parent) {
        this.parent_parameter = parent;
    }

    /**
     * Get the parent parameter map
     *
     * @since    0.1.0
     */
    @Nullable
    public MapParameter<?> getParent() {
        return this.parent_parameter;
    }

    /**
     * Get the root parameter map
     *
     * @since    0.1.0
     */
    @Nullable
    public MapParameter<?> getRootParameter() {
        return this.root_parameter;
    }

    /**
     * Set the root parameter map
     *
     * @since    0.1.0
     */
    @Nullable
    public void setRootParameter(MapParameter<?> root) {
        this.root_parameter = root;
    }

    /**
     * Set the title to use
     *
     * @since    0.1.0
     */
    public TweakParameter<ContainedBvType> setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the title to use
     *
     * @since    0.1.0
     */
    public String getTitle() {

        if (this.title != null) {
            return this.title;
        }

        return this.name;
    }

    /**
     * Extract the value from the given relative map.
     * If it is not directly in the given map, null will be returned.
     * No default value will be returned.
     *
     * @since    0.1.0
     */
    @Nullable
    public ContainedBvType getFromRelativeMap(BvMap map) {

        if (map == null || map.isEmpty()) {
            return null;
        }

        BvElement<?, ?> result = map.get(this.getName());

        return this.castFromDataContext(result);
    }

    /**
     * Find this parameter in the given root map.
     * If this parameter is a child of another parameter,
     * that parent will be fetched first.
     *
     * @since    0.1.0
     */
    @Nullable
    public ContainedBvType getFromRootMap(BvMap map) {

        if (this.parent_parameter == null) {
            return this.getFromRelativeMap(map);
        }

        if (map == null || map.isEmpty()) {
            return null;
        }

        BvMap parent_map = this.parent_parameter.getFromRootMap(map);

        if (parent_map == null) {
            return null;
        }

        return this.getFromRelativeMap(parent_map);
    }

    /**
     * Ensure our parent map exists
     *
     * @since    0.1.0
     */
    @Nullable
    public BvMap getOrCreateContainerInRoot(BvMap root_map) {

        // If there is no root, we can't really do anything
        if (root_map == null) {
            return null;
        }

        // If there is no parent parameter, we should be put in the root
        if (this.parent_parameter == null) {
            return root_map;
        }

        // Get the container of our parent parameter
        BvMap parent_map = this.parent_parameter.ensureMapExistsInRoot(root_map);

        // Something went wrong and our parent parameter failed to ensure its map
        if (parent_map == null) {
            return null;
        }

        return parent_map;
    }

    /**
     * Cast the value before setting it in the data context map
     *
     * @since    0.1.0
     */
    @Nullable
    public BvElement castForDataContextSet(ContainedBvType value) {
        return value;
    }

    /**
     * Cast the value from the data context map
     *
     * @since    0.1.0
     */
    @Nullable
    public ContainedBvType castFromDataContext(BvElement value) {
        return this.castToContainedType(value);
    }

    /**
     * Set the value in the given map
     *
     * @since    0.1.0
     */
    public boolean setInRelativeMap(BvMap map, ContainedBvType value, BvMap root) {

        if (map == null) {
            return false;
        }

        map.put(this.getName(), this.castForDataContextSet(value));

        this.triggerChangeEvent(map, value, root);

        return true;
    }

    /**
     * Set the value in the root map
     *
     * @since    0.1.0
     */
    public boolean setInRootMap(BvMap root_map, ContainedBvType value) {

        if (root_map == null) {
            return false;
        }

        boolean success = false;

        if (this.parent_parameter == null) {
            success = this.setInRelativeMap(root_map, value, root_map);
        } else {
            BvMap parent_map = this.getOrCreateContainerInRoot(root_map);
            success = this.setInRelativeMap(parent_map, value, root_map);
        }

        // The RootTweakMap needs to know it has changed, so it can be saved automatically
        if (success && root_map instanceof RootTweakMap root_tweak_map) {
            root_tweak_map.fireOnChangeListener();
        }

        return success;
    }

    /**
     * Trigger a change event for the current value
     *
     * @since    0.1.0
     */
    public void triggerChangeEvent(BvMap container, ContainedBvType value, BvMap root) {

        if (!this.on_change_listeners.isEmpty()) {
            ChangeContext<ContainedBvType> context = new ChangeContext<>(this, value, root);

            for (var listener : this.on_change_listeners) {
                listener.accept(context);
            }
        }
    }

    /**
     * Add a listener
     *
     * @since    0.1.0
     */
    public TweakParameter<ContainedBvType> addChangeListener(Consumer<ChangeContext<ContainedBvType>> on_change_listener) {
        this.on_change_listeners.add(on_change_listener);
        return this;
    }

    /**
     * Add this parameter to the given command leaf
     *
     * @since    0.1.0
     */
    public CommandLeaf addToCommandLeaf(CommandLeaf parent_leaf) {

        // Create a new child leaf with this parameter's name
        CommandLeaf our_leaf = parent_leaf.getChild(this.name);

        // If this leaf is executed, show the current value
        our_leaf.onExecute(this::performGetValueCommand);

        // Now add the leaf to set a new value
        CommandLeaf set_value_leaf = our_leaf.getChild("set");
        this.configureSetLeaf(set_value_leaf);

        // Also add a "get" leaf
        CommandLeaf get_value_leaf = our_leaf.getChild("get");

        // Actually show the value when executing the "get" command
        get_value_leaf.onExecute(this::performGetValueCommand);

        return our_leaf;
    }

    /**
     * Display our current value to the chat
     *
     * @since    0.1.0
     */
    private int performGetValueCommand(CommandContext<ServerCommandSource> command_context) {
        // Get the command source
        ServerCommandSource source = command_context.getSource();

        // Debug
        source.sendFeedback(() -> Text.literal("Should get value for " + this.name), false);

        // Get the data context
        BvMap data_context = this.getDataContext(command_context);

        if (data_context == null) {
            source.sendError(Text.literal("Failed to find the data context"));
            return 0;
        }

        ContainedBvType value = this.getFromRootMap(data_context);

        MutableText text = Text.literal("Tweak ")
                .append(Text.literal(this.getName()).formatted(Formatting.YELLOW))
                .append(Text.literal(" is currently set to "));

        boolean is_default = false;

        if (value == null) {
            value = this.getDefaultValue();
            is_default = true;
        }

        if (value == null) {
            text = text.append(Text.literal("nothing").formatted(Formatting.GRAY, Formatting.ITALIC));
        } else {
            text = text.append(BvElement.getPrettyText(value));

            if (is_default) {
                text.append(Text.literal(" (default)").formatted(Formatting.GRAY));
            }
        }

        MutableText finalText = text;
        source.sendFeedback(() -> finalText, false);

        return 1;
    }

    /**
     * Configure the "set" part of the commands
     *
     * @since    0.1.0
     */
    public abstract CommandLeaf configureSetLeaf(CommandLeaf set_leaf);

    /**
     * Get the root Map of the given CommandSource
     *
     * @since    0.1.0
     */
    @Nullable
    public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {

        if (command_context == null) {
            return null;
        }

        MapParameter root_map = this.getRootParameter();

        if (root_map == null) {
            return null;
        }

        TweaksConfiguration config = root_map.getTweaksConfiguration();

        if (config == null || config == this) {
            return null;
        }

        return config.getDataContext(command_context);
    }

    /**
     * Return a Arg instance
     *
     * @since    0.1.0
     */
    public BibLog.Arg toBBLogArg() {

        BibLog.Arg result = BibLog.createArg(this);

        result.add("name", this.name);
        result.add("default_value", this.default_value);

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
     * Class to pass when a value changes
     *
     * @since    0.1.0
     */
    public static class ChangeContext<ContainedBvType extends BvElement<?, ?>> {

        private TweakParameter<ContainedBvType> parameter;
        private ContainedBvType value;
        private BvMap root;

        public ChangeContext(TweakParameter<ContainedBvType> parameter, ContainedBvType value, BvMap root) {
            this.parameter = parameter;
            this.value = value;
            this.root = root;
        }

        public TweakParameter<ContainedBvType> getTweakParameter() {
            return this.parameter;
        }

        public ContainedBvType getValue() {
            return this.value;
        }

        public BvMap getRootMap() {
            return this.root;
        }
    }
}
