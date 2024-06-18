package rocks.blackblock.bib.interop;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple class to work with Carpet
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal()
public class InteropCarpet {

    /**
     * Is the given player entity a carpet fake player?
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean isFakePlayer(PlayerEntity player) {

        if (player == null) {
            return false;
        }

        return player instanceof EntityPlayerMPFake;
    }
}
