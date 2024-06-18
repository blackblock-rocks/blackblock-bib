package rocks.blackblock.bib.util;

import net.minecraft.entity.player.PlayerEntity;
import rocks.blackblock.bib.interop.BibInterop;

/**
 * Library class for working with player instances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibPlayer {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibPlayer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Does this player have an explicit permission node set?
     * This will not always be true for an operator.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasExplicitPermission(PlayerEntity player, String permission) {

        if (BibInterop.LUCKPERMS != null) {
            return BibInterop.LUCKPERMS.doesPlayerHaveExplicitPermission(player, permission);
        }

        return false;
    }

    /**
     * Does the given player have the given permission?
     * This will always be true for operators.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasPermission(PlayerEntity player, String permission) {
        // @TODO: implement Luckperms permission check

        if (player != null && player.hasPermissionLevel(2)) {
            return true;
        }

        if (BibInterop.LUCKPERMS != null) {
            return BibInterop.LUCKPERMS.doesPlayerHavePermission(player, permission);
        }

        return false;
    }

    /**
     * Get the given player's username
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String getUsername(PlayerEntity player) {

        if (player == null) {
            return "player-null";
        }

        return player.getNameForScoreboard();
    }
}
