package rocks.blackblock.bib.mixin.performance_info;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import rocks.blackblock.bib.interfaces.HasPerformanceInfo;

/**
 * Placeholder mixin which BibPerf should actually implement
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@Mixin(value = World.class, priority = 1005)
public class WorldMixinForPerformanceInfo implements HasPerformanceInfo {
    // Keep defaults
}
