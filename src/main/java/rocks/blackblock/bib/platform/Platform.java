package rocks.blackblock.bib.platform;

import net.minecraft.entity.player.PlayerEntity;
import rocks.blackblock.bib.util.BibServer;

import java.nio.file.Path;

/**
 * Base class for working with different Platforms
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public abstract class Platform {

    /**
     * Get the path to the directory where config files should be stored
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   The path to the directory where config files should be stored
     */
    public abstract Path getConfigDirPath();

    /**
     * See if a certain mod is loaded
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    mod_id   The identifier of the mod to check for
     */
    public abstract boolean isModLoaded(String mod_id);

    /**
     * Get the live average tick time
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public double getTickTime() {

        if (BibServer.getServer() == null) {
            return 0.0;
        }

        return BibServer.getServer().getAverageTickTime();
    }

    /**
     * Is this player a real player?
     * (Some mods use fake player entities)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public abstract boolean isRealPlayer(PlayerEntity player);
}