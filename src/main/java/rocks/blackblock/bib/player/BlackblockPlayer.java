package rocks.blackblock.bib.player;

/**
 * An injected interface for ServerPlayerEntity
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface BlackblockPlayer {

    /**
     * Is this player stationary?
     * @since    0.2.0
     */
    boolean bb$isStationary();

    /**
     * How many seconds has this player been online?
     * @since    0.2.0
     */
    int bb$getSecondsOnline();

    /**
     * Is this player AFK?
     * @since    0.2.0
     */
    boolean bb$isAfk();
}
