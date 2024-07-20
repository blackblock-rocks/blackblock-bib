package rocks.blackblock.bib.placeholder;

import org.jetbrains.annotations.Nullable;

/**
 * Let this class offer ItemStack replacements
 * for use as a block
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface BlockPlaceholder {

    /**
     * Return the appropriate replacement
     *
     * @since    0.2.0
     */
    @Nullable
    PlaceholderContext.Result getBlockPlaceholderReplacementStack(PlaceholderContext context);
}
