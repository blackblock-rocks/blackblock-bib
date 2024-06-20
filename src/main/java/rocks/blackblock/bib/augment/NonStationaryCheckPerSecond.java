package rocks.blackblock.bib.augment;

import net.minecraft.entity.player.PlayerEntity;

/**
 * The PerUUID & PerPlayer components can also implement this interface
 * to be called every second when the player is not stationary.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.1
 */
public interface NonStationaryCheckPerSecond extends Augment {

    /**
     * Called every second (20 ticks) a player is not stationary
     *
     * @param    player    The player
     */
    void onEveryNonStationarySecond(PlayerEntity player);
}
