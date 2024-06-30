package rocks.blackblock.bib;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.augment.AugmentManager;
import rocks.blackblock.bib.tweaks.TweaksConfiguration;
import rocks.blackblock.bib.config.Config;
import rocks.blackblock.bib.interop.BibInterop;
import rocks.blackblock.bib.platform.FabricPlatform;
import rocks.blackblock.bib.platform.Platform;
import rocks.blackblock.bib.util.BibServer;

@SuppressWarnings("unused")
public class BibMod implements ModInitializer {

	public static final String MOD_ID = "blackblock";

	public static final Logger LOGGER = LogManager.getLogger("BibMod");

	public static final Platform PLATFORM = new FabricPlatform();

	public static final TweaksConfiguration.Global GLOBAL_TWEAKS = new TweaksConfiguration.Global(id("global"));
	public static final TweaksConfiguration.PerPlayer PLAYER_TWEAKS = new TweaksConfiguration.PerPlayer(id("player-tweaks"));
	public static final TweaksConfiguration.PerChunk CHUNK_TWEAKS = new TweaksConfiguration.PerChunk(id("chunk-tweaks"));
	public static final TweaksConfiguration.PerWorld WORLD_TWEAKS = new TweaksConfiguration.PerWorld(id("world-tweaks"));

	@Override
	public void onInitialize() {

		BibInterop.initializeInterops();
		Config.initializeAllConfigs();
		CommandRegistrationCallback.EVENT.register(BibServer::setCommandCanBeRegistered);

		// Initialize the augments when everything has registered
		BibServer.withReadyServer(minecraftServer -> {
			AugmentManager.initialize();
		});
	}

	/**
	 * Create a blackblock identifier
	 *
	 * @author   Jelle De Loecker <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	/**
	 * Get a dynamic registry.
	 * Putting this here for now because I don't know
	 * how important it is to get the correct one in certain places
	 *
	 * @author   Jelle De Loecker <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	@NotNull
	public static RegistryWrapper.WrapperLookup getDynamicRegistry(PlayerEntity player) {

		if (player == null) {
			return getDynamicRegistry();
		}

		return player.getRegistryManager();
	}

	/**
	 * Get the server's dynamic registry
	 *
	 * @author   Jelle De Loecker <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	@NotNull
	public static RegistryWrapper.WrapperLookup getDynamicRegistry() {
		return BibServer.getDynamicRegistry();
	}
}