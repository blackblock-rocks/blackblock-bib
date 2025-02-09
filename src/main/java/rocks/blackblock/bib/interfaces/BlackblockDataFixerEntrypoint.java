package rocks.blackblock.bib.interfaces;

import com.mojang.datafixers.DataFixerBuilder;

/**
 * Let mods register data fixers
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.3.0
 */
public interface BlackblockDataFixerEntrypoint {
    void registerDataFixer(DataFixerBuilder builder);
}
