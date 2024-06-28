package rocks.blackblock.bib.tweaks;

import rocks.blackblock.bib.bv.value.BvMap;

/**
 * A map to use as the root for Tweak settings
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class RootTweakMap extends BvMap {

    protected Runnable on_change_listener = null;

    /**
     * Set the on-change listener
     *
     * @since    0.1.0
     */
    public void setOnChangeListener(Runnable listener) {
        this.on_change_listener = listener;
    }

    /**
     * Fire the change listener
     *
     * @since    0.1.0
     */
    public void fireOnChangeListener() {
        if (this.on_change_listener != null) {
            this.on_change_listener.run();
        }
    }
}
