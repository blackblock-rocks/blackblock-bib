package rocks.blackblock.bib.interfaces;

/**
 * Let something provide disconnection info
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface HasDisconnectionInfo {

    /**
     * Is this thing in the process of disconnecting?
     * (If it already has disconnected it should also return true)
     */
    default boolean bb$isDisconnecting() {
        return false;
    }
}
