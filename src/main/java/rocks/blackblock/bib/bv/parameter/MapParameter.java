package rocks.blackblock.bib.bv.parameter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.tweaks.TweaksConfiguration;
import rocks.blackblock.bib.bv.value.AbstractBvType;
import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.command.CommandLeaf;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A map parameter
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class MapParameter<V extends AbstractBvType<?, ?>>
    extends
        TweakParameter<BvMap>
    implements
        Iterable<TweakParameter<?>> {

    // All the contained parameters
    protected final Map<String, TweakParameter<?>> contained_parameters = new HashMap<>();

    /**
     * Initialize the parameter
     *
     * @since 0.2.0
     */
    public MapParameter(String name) {
        super(name);
    }

    /**
     * Return the class of the contained type
     *
     * @since 0.2.0
     */
    @Override
    public Class<BvMap> getContainedTypeClass() {
        return BvMap.class;
    }

    /**
     * Add a new expected entry
     *
     * @since    0.2.0
     */
    public <T extends BvElement<?, ?>, P extends TweakParameter<T>> P add(P param) {
        this.contained_parameters.put(param.getName(), param);

        param.setParent(this);

        if (this.root_parameter == null) {
            param.setRootParameter(this);
        } else {
            param.setRootParameter(this.root_parameter);
        }

        return param;
    }

    /**
     * Get the root parameter map
     *
     * @since    0.2.0
     */
    @Nullable
    public MapParameter<?> getRootParameter() {

        if (this.root_parameter == null) {
            return this;
        }

        return this.root_parameter;
    }

    /**
     * Get the root TweaksConfiguration
     *
     * @since    0.2.0
     */
    @Nullable
    public TweaksConfiguration getTweaksConfiguration() {

        MapParameter<?> root = this.getRootParameter();

        if (root instanceof TweaksConfiguration tweaks_root) {
            return tweaks_root;
        }

        return null;
    }

    /**
     * Create a new empty map
     *
     * @since    0.2.0
     */
    public BvMap createEmptyMap() {
        return new BvMap();
    }

    /**
     * Ensure a map exist in the given root map
     *
     * @since    0.2.0
     */
    public BvMap ensureMapExistsInRoot(BvMap root_map) {

        if (root_map == null) {
            return null;
        }

        BvMap parent_map;

        if (this.parent_parameter == null) {
            return root_map;
        } else {
            parent_map = this.parent_parameter.ensureMapExistsInRoot(root_map);
        }

        BvMap result = null;

        if (parent_map.containsKey(this.name)) {
            BvElement el = parent_map.get(this.name);

            if (el instanceof BvMap found_map) {
                result = found_map;
            }
        }

        if (result == null) {
            result = new BvMap();
            parent_map.put(this.name, result);
        }

        return result;
    }

    /**
     * Get all the child parameters
     *
     * @since    0.2.0
     */
    public Map<String, TweakParameter<?>> getContainedParameters() {
        return Map.copyOf(this.contained_parameters);
    }

    /**
     * Add an iterator
     *
     * @since    0.2.0
     */
    @NotNull
    @Override
    public Iterator<TweakParameter<?>> iterator() {
        return this.contained_parameters.values().iterator();
    }

    /**
     * Add this parameter to the given command leaf
     *
     * @since    0.2.0
     */
    @Override
    public CommandLeaf addToCommandLeaf(CommandLeaf parent_leaf) {

        CommandLeaf our_leaf = parent_leaf.getChild(this.name);

        for (TweakParameter<?> param : this.contained_parameters.values()) {
            param.addToCommandLeaf(our_leaf);
        }

        return our_leaf;
    }

    /**
     * Configure the "set" part of the commands
     *
     * @since    0.2.0
     */
    public CommandLeaf configureSetLeaf(CommandLeaf set_leaf) {
        return null;
    }

    /**
     * Trigger a change event for the current value
     *
     * @since    0.2.0
     */
    @Override
    public void triggerChangeEvent(BvMap container, BvMap value, BvMap root) {

        // Trigger a change for ourselves
        super.triggerChangeEvent(container, value, root);

        // Trigger a change for all child parameters
        for (TweakParameter child_param : this.contained_parameters.values()) {
            BvElement child_value = child_param.getFromRelativeMap(value);

            if (child_value == null) {
                child_value = child_param.getDefaultValue();
            }

            child_param.triggerChangeEvent(value, child_value, root);
        }
    }
}
