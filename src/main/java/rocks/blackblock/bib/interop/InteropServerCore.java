package rocks.blackblock.bib.interop;

import me.wesley1808.servercore.common.dynamic.DynamicSetting;
import org.jetbrains.annotations.ApiStatus;

/**
 * Class to work with ServerCore
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal()
public class InteropServerCore {

    /**
     * Get the MobcapModifier
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static double getMobcapModifier() {
        int percentage = DynamicSetting.MOBCAP_PERCENTAGE.get();
        return (double) percentage / 100;
    }
}
