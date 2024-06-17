package rocks.blackblock.bib;

import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BibMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("BibMod");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}

	/**
	 * Get a dynamic registry.
	 * Putting this here for now because I don't know
	 * how important it is to get the correct one in certain places
	 *
	 * @since    0.1.0
	 */
	public static RegistryWrapper.WrapperLookup getDynamicRegistry() {
		return DynamicRegistryManager.EMPTY;
	}
}