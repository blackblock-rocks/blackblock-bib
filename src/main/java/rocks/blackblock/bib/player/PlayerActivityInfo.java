package rocks.blackblock.bib.player;

/**
 * An injected interface for ServerPlayerEntity
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface PlayerActivityInfo {

    /**
     * Is this player stationary?
     * @since    0.2.0
     */
    default boolean bb$isStationary() {
        return false;
    }

    /**
     * Mark this player as stationary
     * @since    0.2.0
     */
    default void bb$setIsStationary(boolean stationary) {
        // Ignore
    }

    /**
     * How many seconds has this player been online?
     * @since    0.2.0
     */
    default int bb$getSecondsOnline() {
        return 0;
    }

    /**
     * Is this player AFK?
     * @since    0.2.0
     */
    default boolean bb$isAfk() {
        return false;
    }

    /**
     * Get the ticks since the last movement
     * @since    0.2.0
     */
    default int bb$getTicksSinceLastMovement() {
        return 0;
    }

    /**
     * Should this player be ignored due to system load?
     * @since    0.2.0
     */
    default boolean bb$ignoreDueToSystemLoad() {
        return false;
    }
}
