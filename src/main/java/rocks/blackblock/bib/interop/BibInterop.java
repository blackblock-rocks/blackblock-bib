package rocks.blackblock.bib.interop;

import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.platform.Platform;

/**
 * A class that holds instances of interop classes
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibInterop {

    // Is Spark available?
    public static final boolean HAS_SPARK = BibMod.PLATFORM.isModLoaded("spark");

    // Is Carpet available?
    public static final boolean HAS_CARPET = BibMod.PLATFORM.isModLoaded("carpet");

    // The LuckPerms compatibility instance (if it's loaded)
    public static InteropLuckPerms LUCKPERMS = null;

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibInterop() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Initialize the interop instances
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal()
    public static void initializeInterops() {

        // Get the current platform we're running on
        Platform platform = BibMod.PLATFORM;

        if (platform.isModLoaded("luckperms")) {
            LUCKPERMS = new InteropLuckPerms();
        }
    }
}
