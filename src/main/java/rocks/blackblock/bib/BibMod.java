package rocks.blackblock.bib;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rocks.blackblock.bib.command.CommandCreator;
import rocks.blackblock.bib.config.Config;
import rocks.blackblock.bib.interop.BibInterop;
import rocks.blackblock.bib.platform.FabricPlatform;
import rocks.blackblock.bib.platform.Platform;

public class BibMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("BibMod");

	public static final Platform PLATFORM = new FabricPlatform();

	@Override
	public void onInitialize() {
		BibInterop.initializeInterops();
		Config.initializeAllConfigs();
		CommandRegistrationCallback.EVENT.register(CommandCreator::registerAll);
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