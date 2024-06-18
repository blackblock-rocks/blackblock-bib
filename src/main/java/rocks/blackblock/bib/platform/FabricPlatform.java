package rocks.blackblock.bib.platform;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import rocks.blackblock.bib.interop.BibInterop;
import rocks.blackblock.bib.interop.InteropCarpet;
import rocks.blackblock.bib.interop.InteropSpark;

import java.nio.file.Path;

/**
 * Class to work with the Fabric platform
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.2
 */
@SuppressWarnings("unused")
public class FabricPlatform extends Platform {

    /**
     * Get the path to the directory where config files should be stored
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.2
     *
     * @return   The path to the directory where config files should be stored
     */
    @Override
    public Path getConfigDirPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    /**
     * See if a certain mod is loaded
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.2
     *
     * @param    mod_id   The identifier of the mod to check for
     */
    @Override
    public boolean isModLoaded(String mod_id) {
        return FabricLoader.getInstance().isModLoaded(mod_id);
    }

    /**
     * Get the live average tick time
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public double getTickTime() {

        if (BibInterop.HAS_SPARK) {
            return InteropSpark.getTickTime();
        }

        return super.getTickTime();
    }

    /**
     * Is this player a real player?
     * (Some mods use fake player entities)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public boolean isRealPlayer(PlayerEntity player) {

        if (player instanceof FakePlayer) {
            return false;
        }

        if (BibInterop.HAS_CARPET) {
            if (InteropCarpet.isFakePlayer(player)) {
                return false;
            }
        }

        return true;
    }
}